package boris.com.herramientasmatutuino.config;

import boris.com.herramientasmatutuino.producto.Producto;
import boris.com.herramientasmatutuino.producto.ProductoRepository;
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
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = ProductoRepository.class,
        entityManagerFactoryRef = "productosEntityManagerFactory",
        transactionManagerRef = "productosTransactionManager"
)
public class ProductosDatabaseConfig {
    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.productos")
    public DataSourceProperties productosDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "productosDataSource")
    @Primary
    @ConfigurationProperties("app.datasource.productos.configuration")
    public HikariDataSource productosDataSource(
            @Qualifier("productosDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "productosFlyway", initMethod = "migrate")
    public Flyway productosFlyway(@Qualifier("productosDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/productos")
                .load();
    }

    @Bean(name = "productosEntityManagerFactory")
    @Primary
    @DependsOn("productosFlyway")
    public LocalContainerEntityManagerFactoryBean productosEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("productosDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages(Producto.class)
                .persistenceUnit("productos")
                .build();
    }

    @Bean(name = "productosTransactionManager")
    @Primary
    public JpaTransactionManager productosTransactionManager(
            @Qualifier("productosEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
