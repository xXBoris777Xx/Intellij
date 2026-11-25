package boris.com.herramientasmatutuino.config;

import boris.com.herramientasmatutuino.pedido.Pedido;
import boris.com.herramientasmatutuino.pedido.PedidoRepository;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = PedidoRepository.class,
        entityManagerFactoryRef = "pedidosEntityManagerFactory",
        transactionManagerRef = "pedidosTransactionManager"
)
public class PedidosDatabaseConfig {
    @Bean
    @ConfigurationProperties("app.datasource.pedidos")
    public DataSourceProperties pedidosDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "pedidosDataSource")
    @ConfigurationProperties("app.datasource.pedidos.configuration")
    public HikariDataSource pedidosDataSource(
            @Qualifier("pedidosDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "pedidosFlyway", initMethod = "migrate")
    public Flyway pedidosFlyway(@Qualifier("pedidosDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/pedidos")
                .load();
    }

    @Bean(name = "pedidosEntityManagerFactory")
    @DependsOn("pedidosFlyway")
    public LocalContainerEntityManagerFactoryBean pedidosEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("pedidosDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages(Pedido.class)
                .persistenceUnit("pedidos")
                .build();
    }

    @Bean(name = "pedidosTransactionManager")
    public JpaTransactionManager pedidosTransactionManager(
            @Qualifier("pedidosEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
