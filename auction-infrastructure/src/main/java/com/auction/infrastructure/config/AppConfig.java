package com.auction.infrastructure.config;

import com.auction.infrastructure.dao.AuctionDao;
import com.auction.infrastructure.dao.BidDao;
import com.auction.infrastructure.dao.ItemDao;
import com.auction.infrastructure.dao.UserDao;
import com.auction.infrastructure.event.InMemoryEventBus;
import com.auction.infrastructure.persistence.DataSourceProvider;
import com.auction.infrastructure.persistence.SchemaInitializer;
import com.auction.infrastructure.repository.JdbcAuctionRepository;
import com.auction.infrastructure.repository.JdbcItemRepository;
import com.auction.infrastructure.repository.JdbcUserRepository;
import com.auction.infrastructure.security.BCryptPasswordHasher;
import com.auction.infrastructure.security.InMemoryTokenService;

public class AppConfig {
    private static volatile AppConfig instance;

    private final DataSourceProvider dataSourceProvider;
    private final SchemaInitializer schemaInitializer;

    private final UserDao userDao;
    private final ItemDao itemDao;
    private final AuctionDao auctionDao;
    private final BidDao bidDao;

    private final JdbcUserRepository userRepository;
    private final JdbcItemRepository itemRepository;
    private final JdbcAuctionRepository auctionRepository;

    private final BCryptPasswordHasher passwordHasher;
    private final InMemoryTokenService tokenService;
    private final InMemoryEventBus eventBus;

    private AppConfig() {
        this.dataSourceProvider = DataSourceProvider.getInstance();
        this.schemaInitializer = new SchemaInitializer(this.dataSourceProvider);
        this.schemaInitializer.initialize();

        this.userDao = new UserDao(this.dataSourceProvider);
        this.itemDao = new ItemDao(this.dataSourceProvider);
        this.auctionDao = new AuctionDao(this.dataSourceProvider);
        this.bidDao = new BidDao(this.dataSourceProvider);

        this.userRepository = new JdbcUserRepository(this.userDao);
        this.itemRepository = new JdbcItemRepository(this.itemDao);
        this.auctionRepository = new JdbcAuctionRepository(this.auctionDao, this.bidDao, this.itemDao);

        this.passwordHasher = new BCryptPasswordHasher();
        this.tokenService = new InMemoryTokenService();
        this.eventBus = new InMemoryEventBus();
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    public DataSourceProvider getDataSourceProvider() {
        return dataSourceProvider;
    }

    public SchemaInitializer getSchemaInitializer() {
        return schemaInitializer;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public ItemDao getItemDao() {
        return itemDao;
    }

    public AuctionDao getAuctionDao() {
        return auctionDao;
    }

    public BidDao getBidDao() {
        return bidDao;
    }

    public JdbcUserRepository getUserRepository() {
        return userRepository;
    }

    public JdbcItemRepository getItemRepository() {
        return itemRepository;
    }

    public JdbcAuctionRepository getAuctionRepository() {
        return auctionRepository;
    }

    public BCryptPasswordHasher getPasswordHasher() {
        return passwordHasher;
    }

    public InMemoryTokenService getTokenService() {
        return tokenService;
    }

    public InMemoryEventBus getEventBus() {
        return eventBus;
    }
}