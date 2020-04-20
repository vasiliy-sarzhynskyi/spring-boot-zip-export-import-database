package com.vsarzhynskyi.export_import_db.db;

import com.playtika.test.mariadb.EmbeddedMariaDBBootstrapConfiguration;
import com.playtika.test.mariadb.EmbeddedMariaDBDependenciesAutoConfiguration;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootTest(
        classes = {
                DataSourceAutoConfiguration.class,
                EmbeddedMariaDBDependenciesAutoConfiguration.class,
                EmbeddedMariaDBBootstrapConfiguration.class,
                LiquibaseAutoConfiguration.class,
                PropertyPlaceholderAutoConfiguration.class
        },
        properties = {
                "spring.liquibase.change-log=classpath:liquibase/db/changelog/master.yml",
                "spring.liquibase.contexts=test",
                "spring.datasource.url=jdbc:mariadb://${embedded.mariadb.host}:${embedded.mariadb.port}/${embedded.mariadb.schema}",
                "spring.datasource.username=${embedded.mariadb.user}",
                "spring.datasource.password=${embedded.mariadb.password}"
        }
)
@RunWith(SpringRunner.class)
public class MigrationTest {

    private static final String CHANGE_LOG_FILE = "liquibase/db/changelog/master.yml";

    @Autowired
    protected DataSource dataSource;

    @Test
    public void shouldUpdateAndRollback() throws LiquibaseException, SQLException {
        String liquibaseContext = "test";
        Liquibase liquibase = getLiquibase();

        liquibase.update(liquibaseContext);
        liquibase.rollback(liquibase.getDatabase().getRanChangeSetList().size(), liquibaseContext);
        liquibase.update(liquibaseContext);
    }

    private Liquibase getLiquibase() throws SQLException, LiquibaseException {
        return new Liquibase(
                CHANGE_LOG_FILE,
                new ClassLoaderResourceAccessor(),
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()))
        );
    }

}
