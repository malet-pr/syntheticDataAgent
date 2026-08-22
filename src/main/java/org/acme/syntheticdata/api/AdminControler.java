package org.acme.syntheticdata.api;

import org.acme.syntheticdata.service.DatabaseResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminControler {

    @Autowired
    private DatabaseResetService databaseResetService;

    @GetMapping("/reset-database")
    public Map<String, Object> resetDatabase() {
        return databaseResetService.resetAllTables();
    }

}
