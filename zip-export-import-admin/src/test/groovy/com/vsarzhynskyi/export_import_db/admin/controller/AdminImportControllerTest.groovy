package com.vsarzhynskyi.export_import_db.admin.controller

import com.vsarzhynskyi.export_import_db.admin.service.AdminDataMigrationService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import spock.lang.Specification

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AdminImportController)
@ContextConfiguration(classes = TestConfiguration)
@TestPropertySource(properties = ["admin.importController.enabled=true"])
class AdminImportControllerTest extends Specification {

    @Autowired
    private MockMvc mockMvc
    @SpringBean
    private AdminDataMigrationService adminService = Mock()

    def 'should delete all data'() {
        when:
        def request = mockMvc.perform(MockMvcRequestBuilders.delete('/admin/delete-all-data'))

        then:
        1 * adminService.deleteAllData()
        0 * _

        and:
        request.andExpect(status().isOk())
    }

    def 'should import data from zip file'() {
        given:
        def uploadedFile = new MockMultipartFile('uploadedFile', 'data.zip', 'application/zip', new byte[100])

        when:
        def request = mockMvc.perform(MockMvcRequestBuilders.multipart('/admin/import').file(uploadedFile))

        then:
        1 * adminService.importData(_)
        0 * _

        and:
        request.andExpect(status().isOk())
    }

    def 'should fail import request on passing non-zip file'() {
        given:
        def uploadedFile = new MockMultipartFile('uploadedFile', 'data.json', APPLICATION_JSON_VALUE, new byte[100])

        when:
        def request = mockMvc.perform(MockMvcRequestBuilders.multipart('/admin/import').file(uploadedFile))

        then:
        0 * _

        and:
        request.andExpect(status().isBadRequest())
    }

}
