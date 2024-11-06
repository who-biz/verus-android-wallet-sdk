mod service;
mod service_grpc;

use futures::executor;
use grpc::ClientStub;
use httpbis::ClientTlsOption;
use std::env;
use std::sync::Arc;
use tls_api::{TlsConnector, TlsConnectorBuilder};
use zcash_client_backend::proto::compact_formats;
use zcash_primitives::{merkle_tree::CommitmentTree, sapling::Node};

#[cfg(feature = "mainnet")]
const START_HEIGHT: u64 = 419200;
#[cfg(feature = "mainnet")]
const LIGHTWALLETD_HOST: &str = "lwdlegacy.blur.cash";
#[cfg(feature = "mainnet")]
const NETWORK: &str = "vrsc";

#[cfg(not(feature = "mainnet"))]
const START_HEIGHT: u64 = 280000;
#[cfg(not(feature = "mainnet"))]
const LIGHTWALLETD_HOST: &str = "lightwalletd.testnet.electriccoin.co";
#[cfg(not(feature = "mainnet"))]
const NETWORK: &str = "testnet";

const LIGHTWALLETD_PORT: u16 = 443;
const BATCH_SIZE: u64 = 10_000;
const TARGET_HEIGHT: u64 = 810000;

#[derive(Debug)]
enum Error {
    InvalidBlock,
    Grpc(grpc::Error),
    Io(std::io::Error),
    TlsApi(tls_api::Error),
}

impl From<grpc::Error> for Error {
    fn from(e: grpc::Error) -> Self {
        Error::Grpc(e)
    }
}

impl From<std::io::Error> for Error {
    fn from(e: std::io::Error) -> Self {
        Error::Io(e)
    }
}

impl From<tls_api::Error> for Error {
    fn from(e: tls_api::Error) -> Self {
        Error::TlsApi(e)
    }
}

fn print_sapling_tree(height: u64, mut hash: Vec<u8>, time: u32, tree: CommitmentTree<Node>) {
    hash.reverse();
    let mut tree_bytes = vec![];
    tree.write(&mut tree_bytes).expect("can write into Vec");
    println!("{{");
    println!("  \"network\": \"{}\",", NETWORK);
    println!("  \"height\": {},", height);
    println!("  \"hash\": \"{}\",", hex::encode(hash));
    println!("  \"time\": {},", time);
    println!("  \"tree\": \"{}\"", hex::encode(tree_bytes));
    println!("}}");
}

fn main() -> Result<(), Error> {
    let args: Vec<String> = env::args().collect();
    let mut target_height = TARGET_HEIGHT;

    if args.len() > 1 {
        let target = &args[1];
        match target.parse() {
            Ok(n) => target_height = n,
            Err(_) => {
                eprintln!(
                    "warning: expected target height as the first argument but found '{}'\
                     instead. Falling back to the default target height of {}.",
                    target, target_height
                );
            }
        };
    }

    println!(
        "creating {} checkpoint for range {}..{}",
        NETWORK, START_HEIGHT, target_height
    );
    println!("connecting to {}:{}", LIGHTWALLETD_HOST, LIGHTWALLETD_PORT);

    // For now, start from Sapling activation height
    let mut start_height = START_HEIGHT;
    let mut tree = CommitmentTree::new();

    let tls = {
        let mut tls_connector = tls_api_rustls::TlsConnector::builder()?;

        if tls_api_rustls::TlsConnector::supports_alpn() {
            tls_connector.set_alpn_protocols(&[b"h2"])?;
        }

        let tls_connector = tls_connector.build()?;

        let tls_connector = Arc::new(tls_connector);
        ClientTlsOption::Tls(LIGHTWALLETD_HOST.to_owned(), tls_connector)
    };

    let client = grpc::ClientBuilder::new(LIGHTWALLETD_HOST, LIGHTWALLETD_PORT)
        .explicit_tls(tls)
        .build()
        .map(|c| service_grpc::CompactTxStreamerClient::with_client(Arc::new(c)))?;

    loop {
        // Get the latest height
        let latest_height = target_height;
        let end_height = if latest_height - start_height < BATCH_SIZE {
            latest_height
        } else {
            start_height + BATCH_SIZE - 1
        };

        // Request the next batch of blocks
        println!("Fetching blocks {}..{}", start_height, end_height);
        let mut start = service::BlockID::new();
        start.set_height(start_height);
        let mut end = service::BlockID::new();
        end.set_height(end_height);
        let mut range = service::BlockRange::new();
        range.set_start(start);
        range.set_end(end);
        let blocks = executor::block_on_stream(
            client
                .get_block_range(grpc::RequestOptions::new(), range)
                .drop_metadata(),
        );

        let mut end_hash = vec![];
        let mut end_time = 0;
        let mut parsed = 0;
        for block in blocks {
            let block = block?;
            end_hash = block.hash;
            end_time = block.time;
            for tx in block.vtx.iter() {
                for output in tx.outputs.iter() {
                    // Append commitment to tree
                    let mut repr = [0u8; 32];
                    repr.copy_from_slice(&output.cmu[..]);
                    let cmu: bls12_381::Scalar = Option::from(bls12_381::Scalar::from_bytes(&repr))
                        .ok_or(Error::InvalidBlock)?;
                    let node = Node::new(cmu.to_bytes());
                    tree.append(node).expect("tree is not full");
                }
            }
            parsed += 1
        }
        println!("Parsed {} blocks", parsed);

        if end_height == latest_height {
            print_sapling_tree(end_height, end_hash, end_time, tree);
            break Ok(());
        } else {
            start_height = end_height + 1
        }
    }
}
