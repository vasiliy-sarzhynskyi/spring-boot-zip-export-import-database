package com.vsarzhynskyi.export_import_db.admin.controller

import com.vsarzhynskyi.export_import_db.admin.service.AdminDataMigrationService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import spock.lang.Specification

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AdminExportController)
@ContextConfiguration(classes = TestConfiguration)
class AdminExportControllerTest extends Specification {

    @Autowired
    private MockMvc mockMvc
    @SpringBean
    private AdminDataMigrationService adminService = Mock()

    def 'should export data'() {
        when:
        def request = mockMvc.perform(MockMvcRequestBuilders.get('/admin/export'))

        then:
        1 * adminService.exportData(_) >> { arguments ->
            def outputStream = (ZipOutputStream) arguments[0]
            def zipEntry = new ZipEntry('test.csv')
            outputStream.putNextEntry(zipEntry)
            outputStream.write('test_data'.getBytes())
            outputStream.closeEntry()
        }
        0 * _

        and:
        request.andExpect(status().isOk())
                .andExpect(content().contentType('application/zip'))
    }

}
