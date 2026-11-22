package com.auction.infrastructure.repository;

import com.auction.application.port.AuctionRepository;
import com.auction.contract.vocab.AuctionStatus;
import com.auction.domain.model.auction.Auction;
import com.auction.infrastructure.dao.AuctionDao;
import com.auction.infrastructure.dao.AuctionDao.AuctionRow;
import com.auction.infrastructure.dao.BidDao;
import com.auction.infrastructure.dao.ItemDao;
import com.auction.infrastructure.dao.ItemDao.ItemRow;

import java.util.List;
import java.util.Optional;

public class JdbcAuctionRepository implements AuctionRepository {
    private final AuctionDao auctionDao;
    private final BidDao bidDao;
    private final ItemDao itemDao;

    public JdbcAuctionRepository(AuctionDao auctionDao, BidDao bidDao) {
        this(auctionDao, bidDao, null);
    }

    public JdbcAuctionRepository(AuctionDao auctionDao, BidDao bidDao, ItemDao itemDao) {
        this.auctionDao = auctionDao;
        this.bidDao = bidDao;
        this.itemDao = itemDao;
    }

    @Override
    public Optional<Auction> findById(String auctionId) {
        return auctionDao.findById(auctionId).map(this::mapToDomain);
    }

    @Override
    public List<Auction> findAll() {
        return auctionDao.findAll().stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<Auction> findByStatus(AuctionStatus status) {
        String statusName = status != null ? status.name() : null;
        return auctionDao.findAll().stream()
                .filter(r -> statusName != null && statusName.equals(r.status()))
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public List<Auction> findBySellerId(String sellerId) {
        if (sellerId == null || itemDao == null) {
            return List.of();
        }
        return auctionDao.findAll().stream()
                .filter(r -> itemDao.findById(r.itemId())
                        .map(ItemRow::sellerId)
                        .map(sellerId::equals)
                        .orElse(false))
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public void save(Auction auction) {
        String leaderId = auction.getLeaderId().orElse(null);
        AuctionRow row = new AuctionRow(
                auction.getId(),
                auction.getItemId(),
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getStartingPrice(),
                auction.getCurrentPrice(),
                leaderId,
                auction.getStatus().name(),
                0
        );
        if (auctionDao.findById(auction.getId()).isPresent()) {
            auctionDao.updateState(row);
        } else {
            auctionDao.insert(row);
        }
    }

    @Override
    public void deleteById(String auctionId) {
        auctionDao.findById(auctionId).ifPresent(r -> {
            // Nếu cần xóa hẳn trong DB, bổ sung deleteById vào AuctionDao
        });
    }

    public List<BidDao.BidRow> findBidsByAuctionId(String auctionId) {
        return bidDao.findByAuctionId(auctionId);
    }

    protected Auction mapToDomain(AuctionRow row) {
        // Khi Người 1 thêm Auction.restore(...), gọi khôi phục trạng thái đầy đủ từ row và danh sách bid tại đây
        throw new UnsupportedOperationException(
                "Cần Người 1 bổ sung Auction.restore(...) để khôi phục Auction từ DB cho auctionId=" + row.id()
        );
    }
}