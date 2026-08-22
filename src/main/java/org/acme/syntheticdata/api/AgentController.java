package org.acme.syntheticdata.api;

import org.acme.syntheticdata.dto.SeedRequest;
import org.acme.syntheticdata.service.FullDatabaseAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/agent")
public class AgentController {

    @Autowired
    FullDatabaseAgent fullDatabaseAgent;

    @PostMapping("seed-all-tables")
    public void seedAllTables(@RequestBody SeedRequest dataRequest) throws Exception {
        fullDatabaseAgent.runFullAgent(dataRequest);
    }


}
