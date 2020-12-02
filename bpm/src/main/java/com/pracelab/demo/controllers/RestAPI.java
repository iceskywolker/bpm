package com.pracelab.demo.controllers;

import com.pracelab.demo.dto.*;
import com.pracelab.demo.dto.Requests.CompleteContractStep;
import com.pracelab.demo.dto.Requests.CreateContract;
import com.pracelab.demo.dto.input.ContractData;
import com.pracelab.demo.services.PraceLabFlow;
import com.pracelab.demo.utils.IAuthenticationFacade;
import com.pracelab.demo.utils.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/contracts")
public class RestAPI {
    private Logger logger = LoggerFactory.getLogger(RestAPI.class);

    @Autowired
    private IAuthenticationFacade auth;

    @Autowired
    private PraceLabFlow service;

    @Autowired
    private SecurityUtil securityUtil;


    @GetMapping(path = "/contract_details/{id}", produces = "application/json")
    public ResponseEntity<Contract> getContractDetailsForUserAndContract(@PathVariable String id) {
        try{
            ActivitiUser activitiUser = service.getActivitiUserIfAvailable();

            Contract c = service.getUserContract(id, activitiUser);

            if ( c != null) {
                return ResponseEntity.status(200).body(c);
            }
        }
        catch (Exception ex) {
            logger.warn(ex.getLocalizedMessage(), ex);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Enumerate contract requests for a specific user - user is determined by a Token.
     * @return List<Contract>
     */
    @GetMapping(path = "/contract_details", produces = "application/json")
    public ResponseEntity<List<Contract>> getContractDetailsForUser() {
        List<Contract> contracts = new ArrayList<>();

        try{
            ActivitiUser activitiUser = service.getActivitiUserIfAvailable();
            service.getUserContracts(activitiUser, contracts);
        }
        catch (Exception ex) {
            logger.warn(ex.getLocalizedMessage(), ex);
        }

        return ResponseEntity.status(200).body(contracts);
    }

    @PostMapping(path = "/contract_requests", produces = "application/json")
    public ResponseEntity<Contract> createNewContract(@RequestHeader HttpHeaders reqHeaders, @Valid @RequestBody CreateContract contractRequest) {
        try {
            ActivitiUser activitiUser = service.getActivitiUserIfAvailable();

            if(activitiUser == null) {
                throw new AccessDeniedException("Keycloak token is required!");
            }

            ContractData c = service.getUserContract(contractRequest, activitiUser);
            if ( c != null) {
                logger.info(contractRequest.toString() + " is ALREADY pending as " + c.toString());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            return ResponseEntity.status(HttpStatus.OK).body(service.createContract(contractRequest, activitiUser));
        }
        catch (AccessDeniedException ade) {
            throw ade;
        }
        catch(Exception ex) {
            logger.warn(ex.getLocalizedMessage(), ex);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


    @PutMapping(path = "/complete_request", produces = "application/json")
    public ResponseEntity completeRequestForUser(@RequestHeader HttpHeaders reqHeaders, @Valid @RequestBody CompleteContractStep step) {
        try {
            ActivitiUser activitiUser = service.getActivitiUserIfAvailable();

            if (activitiUser == null) {
                throw new AccessDeniedException("Keycloak token is required!");
            }

            logger.info("Akceptacja user tasku " + step);
            if (!service.completeUserTask(step, activitiUser)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        catch (AccessDeniedException ade) {
            throw ade;
        }
        catch(Exception ex) {
            logger.warn(ex.getLocalizedMessage(), ex);
        }

        return ResponseEntity.status(200).build();
    }

/*
    @GetMapping(path = "/userTest", produces = "application/json")
    public ResponseEntity userTest(@RequestHeader HttpHeaders reqHeaders, HttpServletRequest request) {
        try {
            Principal principal = request.getUserPrincipal();
            logger.info("User " + principal);
            logger.info("User " + auth.getAuthentication());

            if(!auth.getAuthentication().isAuthenticated()) {
                throw new AccessDeniedException("Brak dostepu!!");
            }

            Authentication a = auth.getAuthentication();

            HashSet<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

            for (GrantedAuthority ga : a.getAuthorities())
                authorities.add(ga);

            authorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));



            logger.info("Authorities " + a.getAuthorities());

//            auth.getAuthentication().getAuthorities().add(new SimpleGrantedAuthority("ROLE_USER"));

//            SecurityContext sc = SecurityContextHolder.getContext();
//            sc.setAuthentication(a);

            Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                    20));
            tasks.getContent().forEach(task -> {
                logger.info(">>> task -> " + task);
            });
        }
        catch(AccessDeniedException aex) {
            throw aex;
        }
        catch(Exception ex) {
            logger.warn(ex.getLocalizedMessage(), ex);
        }
        return ResponseEntity.status(200).build();
    }





    @GetMapping("/process-definitions")
    public List<ProcessDefinition> getProcessDefinition(){
        securityUtil.logInAs("prace_lab_api");
        return processRuntime.processDefinitions(Pageable.of(0, 100)).getContent();
    }

    @GetMapping(path = "/getTasksToAccept", produces = "application/json")
    public ResponseEntity<List<TaskDTO>> getInstances(@RequestHeader HttpHeaders reqHeaders) {
        return ResponseEntity.status(200).body(service.getTasksToAccept());
    }

    @GetMapping(path = "/abortTasks", produces = "application/json")
    public ResponseEntity abortTasks(@RequestHeader HttpHeaders reqHeaders) {
        service.abortTasks();
        return ResponseEntity.status(200).build();
    }

    @GetMapping(path = "/listTasks", produces = "application/json")
    public ResponseEntity listTasks(@RequestHeader HttpHeaders reqHeaders) {
        service.listTasks();
        return ResponseEntity.status(200).build();
    }

    @GetMapping(path = "/listUserTasks", produces = "application/json")
    public ResponseEntity listUserTasks(@RequestHeader HttpHeaders reqHeaders) {
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                20));
        tasks.getContent().forEach(task -> {
            logger.info(">>> task -> " + task);
        });
        return ResponseEntity.status(200).build();
    }



    @GetMapping(path = "/getProcessForServiceAndUser", produces = "application/json")
    public ResponseEntity<ProcessInstance> getInstances(@RequestHeader HttpHeaders reqHeaders, @Valid @RequestBody ProcessInfoDTO pInfo) {
        // Tymczasowe podejscie w celach testowych
        securityUtil.logInAs("prace_lab_api");
        // Walidacja czy nie ma juz procesu dla danego id uslugi i uzytkownika, ktory nie jest w stanie COMPLETED
        ProcessInstance pi = getProccessByServiceIdAndUserId(pInfo.getServiceId(), pInfo.getUserID());

        return (pi == null) ? ResponseEntity.status(404).build() : ResponseEntity.status(200).body(pi);
    }

    @PostMapping(path="/orderService", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ActionResponseDTO> orderService(@RequestHeader HttpHeaders reqHeaders, @Valid @RequestBody OrderServiceDTO order) {
        // Tymczasowe podejscie w celach testowych
        securityUtil.logInAs("prace_lab_api");
        // Walidacja czy nie ma juz procesu dla danego id uslugi i uzytkownika, ktory nie jest w stanie COMPLETED
        Page<ProcessInstance> pInstances = processRuntime.processInstances(Pageable.of(0, 20));
        for(ProcessInstance pi : pInstances.getContent()) {
            List<VariableInstance> var = processRuntime.variables(new GetVariablesPayload(pi.getId()));

            if(var != null && !var.isEmpty()) {
                OrderServiceDTO os = (OrderServiceDTO)var.get(0).getValue();

                if(os.getServiceId() == order.getServiceId() && os.getUserID() == order.getUserID() && !pi.getStatus().equals(ProcessInstance.ProcessInstanceStatus.COMPLETED)) {
                    logger.info("Proces dla uslugi o ID " + order.getServiceId() + " i użytkownika " + order.getUserID() + " juz istnieje - przerywam wywolanie.");

                    return ResponseEntity.status(200).body( new ActionResponseDTO(pi.getStatus().toString(), pi.getId(), null));
                }
            }
        }

        logger.info("Start procesu : order-service");

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("Process_order_service")
                .withName("order-service")
                .withVariable("input_data", order)
                .build());
        logger.info(">>> Created Process Instance: " + processInstance);
        return ResponseEntity.status(200).body(new ActionResponseDTO(processInstance.getStatus().toString(), processInstance.getId(), null));

//        return ResponseEntity.status(200).body(service.startExperiment(order));
    }

    @PostMapping(path="/acceptByUser", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ActionResponseDTO> acceptServiceByUser(@RequestHeader HttpHeaders reqHeaders, @Valid @RequestBody AdminAcceptanceDTO action) {
        logger.info("Akceptacja UserTasku dla serwisu: " + action.getServiceId()  + " i uzytkownika : " + action.getUserId() + " ...");
        ProcessInstance pi = getProccessByServiceIdAndUserId(action.getServiceId(), action.getUserId());

        if(pi == null) {
            return ResponseEntity.status(200).body(new ActionResponseDTO("NOT_EXIST", null, null));
        }


        try {
            Page<Task> userTasks = service.getTasks(pi.getId());

            if(userTasks.getTotalItems() > 0) {
                Task task = userTasks.getContent().get(0);

                logger.info("Task "  + task.getName() + "[" + task.getAssignee() + "] => " + task.getStatus());

                taskRuntime.complete(TaskPayloadBuilder
                        .complete()
                        .withTaskId(task.getId())
                        .withVariable("acceptance", action.isAcceptance())
                        .withVariable("deploy_process", action.getProcess())
                        .build());
            }
        }
        catch(ActivitiObjectNotFoundException aex) {
            logger.error(aex.getLocalizedMessage(), aex);
            return ResponseEntity.status(200).body(new ActionResponseDTO("FAILED - brak definicji procesu " + action.getProcess(), pi.getId(), null));
        }
        catch(Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }

        return ResponseEntity.status(200).body(new ActionResponseDTO("FAILED", pi.getId(), null));
    }

    @PostMapping(path="/acceptByAdmin", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ActionResponseDTO> acceptServiceByAdmin(@RequestHeader HttpHeaders reqHeaders, @Valid @RequestBody AdminAcceptanceDTO action) {
        logger.info("Akceptacja UserTasku dla serwisu: " + action.getServiceId()  + " i uzytkownika : " + action.getUserId() + " ...");
        ProcessInstance pi = getProccessByServiceIdAndUserId(action.getServiceId(), action.getUserId());

        if(pi == null) {
            return ResponseEntity.status(200).body(new ActionResponseDTO("NOT_EXIST", null, null));
        }

        return ResponseEntity.status(200).body(service.acceptUserTask(pi, action));
    }

    private ProcessInstance getProccessByServiceIdAndUserId(int serviceId, int userId) {
        Page<ProcessInstance> pInstances = processRuntime.processInstances(Pageable.of(0, 20));
        for(ProcessInstance pi : pInstances.getContent()) {
            List<VariableInstance> var = processRuntime.variables(new GetVariablesPayload(pi.getId()));

            if(var != null && !var.isEmpty()) {
                OrderServiceDTO os = (OrderServiceDTO)var.get(0).getValue();

                if(os.getServiceId() == serviceId && os.getUserID() == userId && !pi.getStatus().equals(ProcessInstance.ProcessInstanceStatus.COMPLETED)) {
                    return pi;
                }
            }
        }

        return null;
    }

    /*private void listProcessVariables(ProcessInstance processInstance) {
        logger.info(">>> Process variables:");
        List<VariableInstance> variables = processRuntime.variables(
                ProcessPayloadBuilder
                        .variables()
                        .withProcessInstance(processInstance)
                        .build());
        variables.forEach(variableInstance -> logger.info("\t> " + variableInstance.getName() + " -> " + variableInstance.getValue()));
    }*/



}
