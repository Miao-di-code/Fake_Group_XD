CREATE TABLE IF NOT EXISTS auto_bids (
    id INTEGER PRIMARY KEY,
    auction_id INTEGER NOT NULL REFERENCES auctions(id),
    bidder_user_id INTEGER NOT NULL REFERENCES users(id),
    max_bid_vnd INTEGER NOT NULL CHECK (max_bid_vnd > 0),
    increment_vnd INTEGER NOT NULL CHECK (increment_vnd > 0),
    created_at_ms INTEGER NOT NULL CHECK (created_at_ms >= 0),
    UNIQUE (auction_id, bidder_user_id)
);
CREATE INDEX IF NOT EXISTS idx_auto_bids_auction ON auto_bids(auction_id, max_bid_vnd DESC, created_at_ms, id);
PRAGMA user_version = 2;
