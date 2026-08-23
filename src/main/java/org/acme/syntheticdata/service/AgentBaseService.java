package org.acme.syntheticdata.service;

import com.google.genai.types.*;
import lombok.extern.slf4j.Slf4j;
import org.acme.syntheticdata.tool.DatabaseExecutionTool;
import org.acme.syntheticdata.tool.DatabaseInspectorTool;
import org.acme.syntheticdata.tool.DatabaseValidationTool;
import org.acme.syntheticdata.tool.EnumsFetchTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class AgentBaseService {

    @Value("${spring.ai.vertex.ai.gemini.project-id}")
    String projectId;

    @Value("${spring.ai.vertex.ai.gemini.location:global}")
    String location;

    @Value("${spring.ai.vertex.ai.gemini.model:gemini-3.5-flash-lite}")
    String model;

    @Autowired
    private DatabaseInspectorTool databaseInspectorTool;

    @Autowired
    private DatabaseExecutionTool databaseExecutionTool;

    @Autowired
    private DatabaseValidationTool databaseValidationTool;

    @Autowired
    private EnumsFetchTool enumsFetchTool;

    private List<Method> scanToolMethods(Class<?>... toolClasses) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> clazz : toolClasses) {
            for (Method method : clazz.getDeclaredMethods()) {
                boolean isTool = Arrays.stream(method.getAnnotations())
                        .anyMatch(a -> a.annotationType().getSimpleName().equals("Tool"));
                // Google SDK requires public static methods
                if (isTool && Modifier.isStatic(method.getModifiers())) {
                    methods.add(method);
                    log.info("[TOOL REGISTERED] Registered static method: {}.{}()",
                            clazz.getSimpleName(), method.getName());
                }
            }
        }
        if (methods.isEmpty()) {
            throw new IllegalStateException("No public static @Tool methods found in provided classes!");
        }
        return methods;
    }

    private String loadSkillFile(String resourceName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) return "";
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load skills file", e);
        }
    }

    GenerateContentConfig configClient() {
        List<Method> toolMethods = scanToolMethods(
                DatabaseInspectorTool.class,
                DatabaseExecutionTool.class,
                DatabaseValidationTool.class,
                EnumsFetchTool.class
        );
        String systemSkills = loadSkillFile("skillsDraft.md");
        AutomaticFunctionCallingConfig autoConfig = AutomaticFunctionCallingConfig.builder()
                .maximumRemoteCalls(25)
                .build();
        FunctionCallingConfig functionCallingConfig =
                FunctionCallingConfig.builder()
                        .mode(FunctionCallingConfigMode.Known.AUTO)
                        .build();
        ToolConfig toolConfig =
                ToolConfig.builder()
                        .functionCallingConfig(functionCallingConfig)
                        .build();
        return GenerateContentConfig.builder()
                .systemInstruction(Content.builder()
                        .parts(List.of(Part.fromText(systemSkills)))
                        .build())
                .automaticFunctionCalling(autoConfig)
                .toolConfig(toolConfig)
                .tools(List.of(
                        Tool.builder().functions(toolMethods.toArray(new Method[0])).build()
                ))
                .build();
    }

}
