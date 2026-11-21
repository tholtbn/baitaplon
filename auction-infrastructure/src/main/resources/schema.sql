CREATE TABLE IF NOT EXISTS users (
                                     id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(64) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL
    );

CREATE TABLE IF NOT EXISTS items (
                                     id VARCHAR(64) PRIMARY KEY,
    seller_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    starting_price DECIMAL(19, 4) NOT NULL,
    type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL
    );

CREATE TABLE IF NOT EXISTS auctions (
                                        id VARCHAR(64) PRIMARY KEY,
    item_id VARCHAR(64) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    starting_price DECIMAL(19, 4) NOT NULL,
    current_price DECIMAL(19, 4) NOT NULL,
    leader_id VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    version INT NOT NULL
    );

CREATE TABLE IF NOT EXISTS bids (
                                    id VARCHAR(64) PRIMARY KEY,
    auction_id VARCHAR(64) NOT NULL,
    bidder_id VARCHAR(64) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    timestamp TIMESTAMP NOT NULL
    );