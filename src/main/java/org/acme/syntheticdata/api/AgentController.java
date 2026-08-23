package org.acme.syntheticdata.api;

import org.acme.syntheticdata.dto.SeedRequest;
import org.acme.syntheticdata.service.DatabaseAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/agent")
public class AgentController {

    @Autowired
    DatabaseAgentService databaseAgentService;

    @PostMapping("seed-all-tables")
    public String seedAllTables(@RequestBody SeedRequest dataRequest) throws Exception {
       return databaseAgentService.runFullAgent(dataRequest);
    }


}
