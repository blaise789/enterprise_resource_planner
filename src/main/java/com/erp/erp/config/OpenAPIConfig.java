package com.erp.erp.config;


import com.erp.erp.enums.ERole;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        // Define servers
        Server localServer = new Server()
                .url("http://localhost:7000")
                .description("Local Development Server");

        Server productionServer = new Server()
                .url("https://erp-api.example.com")
                .description("Production Server");

        // Define tags for API grouping
        List<Tag> tags = Arrays.asList(
                new Tag().name("Authentication").description("Operations related to user authentication"),
                new Tag().name("Employee Management").description("Operations related to employee management"),
                new Tag().name("Payroll").description("Operations related to payroll processing"),
                new Tag().name("Admin").description("Administrative operations")
        );

        // Create schema for email validation
        Schema<?> emailSchema = new StringSchema()
                .description("Email address")
                .pattern("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
                .example("user@example.com");

        // Create schema for role selection with enum values
        Schema<?> roleSchema = new StringSchema()
                .description("User role")
                ._enum(Arrays.stream(ERole.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .example(ERole.ROLE_EMPLOYEE.name());

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("Enter JWT Bearer token **_only_**")
                                )
                                .addSchemas("EmailSchema", emailSchema)
                                .addSchemas("RoleSchema", roleSchema)
                )
                .info(new Info()
                        .title("ERP System API")
                        .version("v1.0")
                        .description("Backend API for the Government of Rwanda ERP System Payroll and Employee Management. " +
                                "This API provides endpoints for managing employees, processing payroll, and administrative functions.")
                        .contact(new Contact()
                                .name("ERP Support Team")
                                .email("support@erp-system.com")
                                .url("https://erp-system.com/support"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                )
                .servers(Arrays.asList(localServer, productionServer))
                .tags(tags);
    }
}
