package com.auction.infrastructure.config;

import com.auction.application.concurrency.AuctionLockRegistry;
import com.auction.application.port.IdGenerator;
import com.auction.application.port.TokenService;
import com.auction.application.service.AuctionService;
import com.auction.application.service.AuthService;
import com.auction.application.service.BiddingService;
import com.auction.application.service.ItemService;
import com.auction.infrastructure.event.InMemoryEventBus;
import com.auction.infrastructure.persistence.DataSourceProvider;
import com.auction.infrastructure.persistence.SchemaInitializer;
import com.auction.infrastructure.repository.JdbcAuctionRepository;
import com.auction.infrastructure.repository.JdbcItemRepository;
import com.auction.infrastructure.repository.JdbcUserRepository;
import com.auction.infrastructure.security.BCryptPasswordHasher;
import com.auction.infrastructure.security.InMemoryTokenService;
import com.auction.infrastructure.security.UuidGenerator;

import java.time.Clock;

public class AppConfig {
    private static AppConfig instance;

    private final DataSourceProvider dataSourceProvider;
    private final SchemaInitializer schemaInitializer;
    private final JdbcUserRepository userRepository;
    private final JdbcItemRepository itemRepository;
    private final JdbcAuctionRepository auctionRepository;
    private final BCryptPasswordHasher passwordHasher;
    private final InMemoryEventBus eventBus;
    private final AuctionLockRegistry lockRegistry;
    private final Clock clock;

    private final AuthService authService;
    private final BiddingService biddingService;
    private final AuctionService auctionService;
    private final ItemService itemService;

    private AppConfig() {
        this.dataSourceProvider = DataSourceProvider.getInstance();
        this.schemaInitializer = new SchemaInitializer(dataSourceProvider);
        this.schemaInitializer.initialize();

        this.userRepository = new JdbcUserRepository(dataSourceProvider);
        this.itemRepository = new JdbcItemRepository(dataSourceProvider);
        this.auctionRepository = new JdbcAuctionRepository(dataSourceProvider);
        this.passwordHasher = new BCryptPasswordHasher();
        this.eventBus = new InMemoryEventBus();
        this.lockRegistry = new AuctionLockRegistry();
        this.clock = Clock.systemUTC();

        IdGenerator idGenerator = new UuidGenerator();
        TokenService tokenService = new InMemoryTokenService();

        this.authService = new AuthService(userRepository, passwordHasher, tokenService, idGenerator);
        this.biddingService = new BiddingService(auctionRepository, lockRegistry, eventBus, idGenerator, clock);
        this.auctionService = new AuctionService(auctionRepository, itemRepository, idGenerator, clock);
        this.itemService = new ItemService(itemRepository, auctionRepository);
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public AuthService getAuthService() { return authService; }
    public BiddingService getBiddingService() { return biddingService; }
    public AuctionService getAuctionService() { return auctionService; }
    public ItemService getItemService() { return itemService; }
    public InMemoryEventBus getEventBus() { return eventBus; }
}