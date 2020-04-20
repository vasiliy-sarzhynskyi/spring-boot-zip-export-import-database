package com.vsarzhynskyi.export_import_db.jpa.repository

import com.vsarzhynskyi.export_import_db.jpa.entity.FirstEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.context.transaction.TransactionalTestExecutionListener
import spock.lang.Specification

import javax.transaction.Transactional

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE

@DataJpaTest
@Transactional
@ActiveProfiles("repositoryTest")
@TestPropertySource(properties = [
        "embedded.mariadb.enabled=false",
        "liquibase.enabled=true"])
@AutoConfigureTestDatabase(replace = NONE)
@TestExecutionListeners([DependencyInjectionTestExecutionListener, TransactionalTestExecutionListener])
class FirstRepositoryTest extends Specification {

    @Autowired
    private FirstRepository repository

    @Autowired
    private TestEntityManager entityManager

    def 'should save entity and return back'() {
        given:
        def entityToSave = FirstEntity.builder()
                .description('test')
                .build()
        def savedId = repository.saveAndFlush(entityToSave).id

        when:
        entityManager.clear()
        def foundEntityOptional = repository.findById(savedId)

        then:
        foundEntityOptional.isPresent()
        def foundEntity = foundEntityOptional.get()
        foundEntity.id == savedId
        foundEntity.createdAt
        foundEntity.updatedAt
    }

}
