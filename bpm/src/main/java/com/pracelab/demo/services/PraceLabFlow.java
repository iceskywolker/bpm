package com.pracelab.demo.services;

import com.pracelab.demo.dto.*;
import com.pracelab.demo.dto.Requests.CompleteContractStep;
import com.pracelab.demo.dto.Requests.CreateContract;
import com.pracelab.demo.dto.input.ContractData;
import com.pracelab.demo.utils.IAuthenticationFacade;
import com.pracelab.demo.utils.SecurityUtil;
import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.DeleteProcessPayloadBuilder;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.CompleteTaskPayloadBuilder;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.api.task.runtime.events.TaskAssignedEvent;
import org.activiti.api.task.runtime.events.TaskCompletedEvent;
import org.activiti.api.task.runtime.events.listener.TaskRuntimeEventListener;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PraceLabFlow {
    private Logger logger = LoggerFactory.getLogger(PraceLabFlow.class);

    @Autowired
    private IAuthenticationFacade auth;

    /**
     * Usluga do zarzadzania UserTaskami - wymagana rola ACTIVITI_USER
     */
    @Autowired
    private TaskRuntime taskRuntime;

    /**
     * Usluga do zarzadzania Procesami - wymagana rola ACTIVITI_USER
     */
    @Autowired
    private ProcessRuntime processRuntime;
    /**
     * Usluga do zarzadzania UserTaskami - wymagana rola ACTIVITI_ADMIN
     */
    @Autowired
    private TaskAdminRuntime taskARuntime;

    @Autowired
    private ProcessEngine pe;

    @Autowired
    private HistoryService hs;
    /**
     * Usluga do zarzadzania Procesami - wymagana rola ACTIVITI_ADMIN
     */
    @Autowired
    private ProcessAdminRuntime processARuntime;

    @Autowired
    private SecurityUtil securityUtil;

    private List<VariableCreatedEvent> variableCreatedEvents = new ArrayList<>();

    private List<ProcessCompletedEvent> processCompletedEvents = new ArrayList<>();

    public PraceLabFlow() {

    }

    public ActivitiUser getActivitiUserIfAvailable() {
        if(auth.getAuthentication().getDetails() instanceof SimpleKeycloakAccount) {
            SimpleKeycloakAccount ska = (SimpleKeycloakAccount)auth.getAuthentication().getDetails();
            return new ActivitiUser(ska);
        }

        return null;
    }

// ------------------ Operacje na silniku BPM, etc --------------------------------------------
    public void getUserContracts(ActivitiUser user, List<Contract> contracts) throws AccessDeniedException {
        securityUtil.logInAs("admin_prace_lab");

        Page<ProcessInstance> pInstances = processARuntime.processInstances(Pageable.of(0, 20));

        for(ProcessInstance pi : pInstances.getContent()) {

            Map<String, Object> vars = pe.getRuntimeService().getVariables(pi.getId());

            if(vars != null && vars.containsKey("input_data")) {
                ContractData cData = (ContractData)vars.get("input_data");
                logger.info("Dane " + cData);

                if(cData.getActivitiUser().equals(user.getPrefferedUserName())) {
                    UserStep us = getUserStepForProcessIfAny(pi.getId(), user);
                    contracts.add(new Contract(pi.getId(), pi.getStatus().toString(), us));
                }
            }
        }
    }

    public Contract getUserContract(String processId, ActivitiUser activitiUser) throws AccessDeniedException, NotFoundException {
        Contract c = null;

        logger.info("Sprawdzanie czy sa trwajace kontrakty dla uzytkownika: " + auth.getAuthentication());

        securityUtil.logInAs("admin_prace_lab");

        ProcessInstance pi = processARuntime.processInstance(processId);

        if(pi == null) {
            return null;
        }

        Map<String, Object> vars = pe.getRuntimeService().getVariables(pi.getId());

        if(vars != null && vars.containsKey("input_data")) {
            ContractData cData = (ContractData)vars.get("input_data");
            logger.info("Dane " + cData);

            if(cData.getActivitiUser().equals(activitiUser.getPrefferedUserName())) {
                UserStep us = getUserStepForProcessIfAny(pi.getId(), activitiUser);
                c  = new Contract(pi.getId(), pi.getStatus().toString(), us);
            }
        }

        return c;
    }

    public ContractData getUserContract(CreateContract request, ActivitiUser activitiUser) throws AccessDeniedException {
        ContractData uc = null;

        logger.info("Sprawdzanie czy sa trwajace kontrakty dla uzytkownika: " + auth.getAuthentication());

        securityUtil.logInAs("admin_prace_lab");

        Page<ProcessInstance> pInstances = processARuntime.processInstances(Pageable.of(0, 20));

        for(ProcessInstance pi : pInstances.getContent()) {

            Map<String, Object> vars = pe.getRuntimeService().getVariables(pi.getId());

            if(vars != null && vars.containsKey("input_data")) {
                ContractData cData = (ContractData)vars.get("input_data");
                logger.info("Dane " + cData);

                if(cData.getActivitiUser().equals(activitiUser.getPrefferedUserName()) && cData.getContractRequest().getContractType().equals(request.getContractType())) {
                    uc = cData;
                    break;
                }
            }
        }

//        logger.info("User : " + ((SimpleKeycloakAccount)auth.getAuthentication().getDetails()).getPrincipal() + " : " + ((SimpleKeycloakAccount)auth.getAuthentication().getDetails()).getRoles());
//        logger.info("UserN : " + auth.getAuthentication());
//
//        logger.info("TOKEN: " + ((SimpleKeycloakAccount)auth.getAuthentication().getDetails()).getKeycloakSecurityContext().getToken().issuedFor);

        return uc;
    }

    public boolean isUserTaskEligible(CompleteContractStep step, ActivitiUser user) {
        try {
            securityUtil.logInAs("admin_prace_lab");
            Page<Task> userTasks = taskARuntime.tasks(Pageable.of(0,
                    50));
            for(Task t : userTasks.getContent()) {
                if(t.getId().equals(step.getStepId()) && t.getAssignee().equals(user.getPrefferedUserName())) {
                    return true;
                }
            }
        }
        catch(Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }

        return false;
    }

    protected boolean checkIfTaskExist(String taskId) {
        try {
            securityUtil.logInAs("admin_prace_lab");
            return (taskARuntime.task(taskId) != null);
        }
        catch(Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }

        return false;
    }

    public boolean completeUserTask(CompleteContractStep step, ActivitiUser user) throws AccessDeniedException, HttpClientErrorException.NotFound {
        if(!isUserTaskEligible(step, user)) throw new AccessDeniedException("User has no eligibility to such taskID");

//        if(!checkIfTaskExist(step.getStepId()))
//            return false;

        securityUtil.logInAs("admin_prace_lab");

        logger.info("Akceptacja : " + taskARuntime.complete(new CompleteTaskPayloadBuilder()
                .withTaskId(step.getStepId())
                .build()));

        return true;
    }

    public Contract createContract(CreateContract request, ActivitiUser user) {
        Contract uc = null;

        try {
            // Dzialania na uprawnieniach admina, TODO
            securityUtil.logInAs("admin_prace_lab");
            logger.info("Dane wejsciowe: " + request);

            ProcessInstance processInstance = processARuntime.start(ProcessPayloadBuilder
                    .start()
                    .withProcessDefinitionKey("Process_l2exdsKWx")
                    .withName("request-student-contract")
                    .withVariable("input_data", new ContractData(request, user.getPrefferedUserName(), user.getRoles()))
                    .withVariable("assignee", user.getPrefferedUserName())
                    .build());
            logger.info(">>> Created Process Instance: " + processInstance);

            uc = new Contract(processInstance.getId(), processInstance.getStatus().toString(), null);
        }
        catch(Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }

        return uc;
    }

    private UserStep getUserStepForProcessIfAny(String pid, ActivitiUser user){
        try {
            securityUtil.logInAs("admin_prace_lab");
            Page<Task> userTasks = taskARuntime.tasks(Pageable.of(0,
                    50));
            for(Task t : userTasks.getContent()) {
                if(t.getProcessInstanceId().equals(pid) && t.getAssignee().equals(user.getPrefferedUserName())) {
                    String jsonSchema = "{" +
                            "  \"title\": \"A registration form\"," +
                            "  \"description\": \"A simple form example.\"," +
                            "  \"type\": \"object\"," +
                            "  \"required\": [" +
                            "    \"firstName\"," +
                            "    \"lastName\"" +
                            "  ]," +
                            "  \"properties\": {" +
                            "    \"firstName\": {" +
                            "      \"type\": \"string\"," +
                            "      \"title\": \"First name\"," +
                            "      \"default\": \"Chuck\"" +
                            "    }," +
                            "    \"lastName\": {" +
                            "      \"type\": \"string\"," +
                            "      \"title\": \"Last name\"" +
                            "    }," +
                            "    \"telephone\": {" +
                            "      \"type\": \"string\"," +
                            "      \"title\": \"Telephone\"," +
                            "      \"minLength\": 10" +
                            "    }" +
                            "  }" +
                            "}";
                    String uiSchema = "{" +
                            "  \"firstName\": {" +
                            "    \"ui:autofocus\": true," +
                            "    \"ui:emptyValue\": \"\"," +
                            "    \"ui:autocomplete\": \"family-name\"" +
                            "  }," +
                            "  \"lastName\": {" +
                            "    \"ui:emptyValue\": \"\"," +
                            "    \"ui:autocomplete\": \"given-name\"" +
                            "  }," +
                            "  \"age\": {" +
                            "    \"ui:widget\": \"updown\"," +
                            "    \"ui:title\": \"Age of person\"," +
                            "    \"ui:description\": \"(earthian year)\"" +
                            "  }," +
                            "  \"bio\": {" +
                            "    \"ui:widget\": \"textarea\"" +
                            "  }," +
                            "  \"password\": {" +
                            "    \"ui:widget\": \"password\"," +
                            "    \"ui:help\": \"Hint: Make it strong!\"" +
                            "  }," +
                            "  \"date\": {" +
                            "    \"ui:widget\": \"alt-datetime\"" +
                            "  }," +
                            "  \"telephone\": {" +
                            "    \"ui:options\": {" +
                            "      \"inputType\": \"tel\"" +
                            "    }" +
                            "  }" +
                            "}";
                    String formData = "{" +
                            "  \"firstName\": \"" + user.getPrefferedUserName() + "\"," +
                            "}";

                    return new UserStep(t.getId(), t.getName(), jsonSchema, uiSchema, formData, "/complete_request");
                }
            }
        }
        catch(Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }

        return null;
    }

    public ActionResponseDTO startExperiment(OrderServiceDTO order) {
        try {
            // Swiadoma korekta uprawnien w celach testowych ;-)
            securityUtil.logInAs("admin_prace_lab");
            logger.info("Dane wejsciowe: " + order);

            ProcessInstance processInstance = processARuntime.start(ProcessPayloadBuilder
                    .start()
                    .withProcessDefinitionKey("Process_order_service")
                    .withName("order-service")
                    .withVariable("input_data", order)
                    .build());
            logger.info(">>> Created Process Instance: " + processInstance);
            return new ActionResponseDTO(processInstance.getStatus().toString(), processInstance.getId(), null);
        }
        catch(Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }

        return new ActionResponseDTO("FAILED", null, null);
    }

    public Page<Task> getTasks(String PID) {
        return taskARuntime.tasks(Pageable.of(0, 20), TaskPayloadBuilder.tasks().withProcessInstanceId(PID).build());
    }

    public ActionResponseDTO acceptUserTask(ProcessInstance pi, AdminAcceptanceDTO dto) {
        try {
            // Swiadoma korekta uprawnien w celach testowych ;-)
            securityUtil.logInAs("admin_prace_lab");
            Page<Task> userTasks = taskARuntime.tasks(Pageable.of(0, 20), TaskPayloadBuilder.tasks().withProcessInstanceId(pi.getId()).build());
            logger.info("Istnieje " + userTasks.getTotalItems() + " oczekujacych zadan uzytkownika");

            if(userTasks.getTotalItems() > 0) {
                Task task = userTasks.getContent().get(0);
                logger.info("Zaakceptowano zadanie " + task.getName() + "[" + task.getId() + "] z rezultatem: " + ((dto.isAcceptance())? "zatwierdzono usługę" : "odrzucono usługę"));
                taskARuntime.complete(TaskPayloadBuilder
                        .complete()
                        .withTaskId(task.getId())
                        .withVariable("acceptance", dto.isAcceptance())
                        .withVariable("deploy_process", dto.getProcess())
                        .build());


                return new ActionResponseDTO("COMPLETED", pi.getId(), task.getId());
            }
        }
        catch(ActivitiObjectNotFoundException aex) {
            logger.error(aex.getLocalizedMessage(), aex);
            return new ActionResponseDTO("FAILED - brak definicji procesu " + dto.getProcess(), pi.getId(), null);
        }
        catch(Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }

        return new ActionResponseDTO("FAILED", pi.getId(), null);
    }

    public void abortTasks() {
        try {
            logger.info("Pobieranie taskow...");
            // Jesli ponizsza linijka zostanie odkomentowana, to mechanizmy bezpieczenstwa przepuszcza kazdego uzytkownika z rola ACTIVITI_USER lub ACTIVITI_ADMIN (tak sa zdefiniowane constrainty)
            // W przeciwnym przypadku wywolanie metody bedzie dopuszczone dla kazdego uzytkownika z rola ACTIVITI_USER lub ACTIVITI_ADMIN, ale wywolanie processARuntime.processInstances... zwroci wyjatek w przypadku roli uzytkownika ACTIVITI_USER.
            securityUtil.logInAs("admin_prace_lab");
            Page<Task> userTasks = taskARuntime.tasks(Pageable.of(0,
                    50));
            userTasks.getContent().forEach(task -> {
                logger.info(task.getName() + " : owner = " + task.getOwner() + " @ assignee: " + task.getAssignee() + " : " + task.getStatus());
                processARuntime.delete(new DeleteProcessPayloadBuilder().withProcessInstanceId(task.getProcessInstanceId()).build());
//                taskARuntime.delete(new DeleteTaskPayloadBuilder().withTaskId(task.getId()).build());
            });
        }
        catch(Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }
    }

    public List<TaskDTO> getTasksToAccept() {
        List<TaskDTO> tasks = new ArrayList<>();
        try {
            logger.info("Pobieranie taskow...");
            // Jesli ponizsza linijka zostanie odkomentowana, to mechanizmy bezpieczenstwa przepuszcza kazdego uzytkownika z rola ACTIVITI_USER lub ACTIVITI_ADMIN (tak sa zdefiniowane constrainty)
            // W przeciwnym przypadku wywolanie metody bedzie dopuszczone dla kazdego uzytkownika z rola ACTIVITI_USER lub ACTIVITI_ADMIN, ale wywolanie processARuntime.processInstances... zwroci wyjatek w przypadku roli uzytkownika ACTIVITI_USER.
            securityUtil.logInAs("admin_prace_lab");
            Page<Task> userTasks = taskARuntime.tasks(Pageable.of(0,
                    50));
            userTasks.getContent().forEach(task -> {
                tasks.add(new TaskDTO(task.getProcessInstanceId(), task.getId(), task.getName(), task.getTaskDefinitionKey(), task.getStatus().toString(), getTaskData(task.getId())));
            });
        }
        catch(Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }

        return tasks;
    }

    private OrderServiceDTO getTaskData(String taskId) {
        VariableInstance vi = taskARuntime.variables(TaskPayloadBuilder
                .variables()
                .withTaskId(taskId)
                .build()).stream()
                .filter(variableInstance -> "input_data".equals(variableInstance.getName()))
                .findAny()
                .orElse(null);
        return (vi != null)? vi.getValue() : null;
    }

    private void listTaskVariables(Task task) {
       logger.info(">>> Task variables:");
        taskARuntime.variables(TaskPayloadBuilder
                .variables()
                .withTaskId(task.getId())
                .build())
                .forEach(variableInstance -> logger.info("\t> " + variableInstance.getName() + " -> " + variableInstance.getValue()));
    }

    public void listTasks() {
        securityUtil.logInAs("admin_prace_lab");
        Page<Task> tasks = taskARuntime.tasks(Pageable.of(0,
                20));
        tasks.getContent().forEach(task -> {
            logger.info(">>> task -> " + task);
        });
    }

    public void listCompletedProcesses() {
        logger.info(">>> Completed process Instances: ");
        processCompletedEvents.forEach(processCompletedEvent -> logger.info("\t> Process instance : " + processCompletedEvent.getEntity()));
    }

    private void listAvailableProcesses() {
        Page<ProcessDefinition> processDefinitionPage = processARuntime.processDefinitions(Pageable.of(0,
                10));
        logger.info("> Available Process definitions: " + processDefinitionPage.getTotalItems());
        for (ProcessDefinition pd : processDefinitionPage.getContent()) {
            logger.info("\t > Process definition: " + pd);
        }
    }

    public void getProcessInstances() {
        securityUtil.logInAs("admin_prace_lab");
        logger.info(">>> Process Instances: ");
        Page<ProcessInstance> pInstances = processARuntime.processInstances(Pageable.of(0, 20));
        pInstances.getContent().forEach(pi -> {
            logger.info(pi.getId() + "[" + pi.getName() + "]" + pi.getStatus().toString());
            /*if(pi.getStatus().equals(ProcessInstance.ProcessInstanceStatus.RUNNING)) {
                logger.info("Usypianie procesu : " + pi.getName() + "...");
                processARuntime.suspend(new SuspendProcessPayloadBuilder().withProcessInstance(pi).build());
            }*/

          /*  if(pi.getStatus().equals(ProcessInstance.ProcessInstanceStatus.RUNNING)) {
                logger.info("Ubijanie procesu : " + pi.getName() + "...");
                processARuntime.delete(new DeleteProcessPayloadBuilder().withProcessInstance(pi).build());
            }
*/
            /*if(pi.getStatus().equals(ProcessInstance.ProcessInstanceStatus.SUSPENDED)) {
                logger.info("Wznawianie procesu : " + pi.getName() + "...");
                processARuntime.resume(new ResumeProcessPayloadBuilder().withProcessInstance(pi).build());
            }*/
        });
    }

    public void testPE() {
        logger.info("PE...");
        try {
            TaskService taskService = pe.getTaskService();
            List<org.activiti.engine.task.Task> tasks = taskService.createTaskQuery().taskUnassigned().list();
            for (org.activiti.engine.task.Task t : tasks) {
                logger.info(t.getName() + " : " + t.getOwner() + " : " + t.getAssignee());
            }

            List<HistoricProcessInstance> ls = hs.createHistoricProcessInstanceQuery().deleted().list();
            for (HistoricProcessInstance h : ls) {
                logger.info(h.getName() + " : " + h.getDeleteReason());
            }

            ls = hs.createHistoricProcessInstanceQuery().finished().list();

            for (HistoricProcessInstance h : ls) {
                logger.info(h.getName() + " : " + " | " + ls);
            }

            ls = hs.createNativeHistoricProcessInstanceQuery().list();

            for (HistoricProcessInstance h : ls) {
                logger.info(h.getName() + " : " + " | " + ls);
            }
        }
        catch(Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }
    }

    // ------------------ Przyklad implementacji konektora, ktory jest zintegrowany z glownym srodowiskiem uruchomieniowym aplikacji. Dla takiej implementacji wywolania nie odbywaja sie w oparciu o strumienie i system kolejkowy  --------------------------------------------
    @Bean("verifyAffiliationSrv")
    public Connector getVerifyAffilioationConnector() {
        return integrationContext -> {
            logger.info("------------------------------------------------------------------------------");
            String var1 = this.getClass().getSimpleName()+" was called for instance " + integrationContext.getProcessInstanceId() + "[Business Key:" + integrationContext.getBusinessKey() + "] ClientName: " + integrationContext.getClientName() + ", ClientId: " + integrationContext.getClientId() + ", ProcessInstanceId: " +  integrationContext.getProcessInstanceId() + ", ParentProcessInstanceId: " + integrationContext.getParentProcessInstanceId();

            logger.info("[" + integrationContext.getId() + " : " + integrationContext.getProcessInstanceId() + " : " + integrationContext.getProcessDefinitionId() + " : " + integrationContext.getConnectorType() + "]");

            logger.info(var1);
            logger.info("Zmienne : " + integrationContext.getInBoundVariables());

            ContractData cData = integrationContext.getInBoundVariable("input_data");

            logger.info("Zatwierdzanie afiliacji");
            integrationContext.getOutBoundVariables().put("affiliation_verified", true);

            logger.info("Zapisywanie stanu obiektu do DB...");
            logger.info("Proces zostal zakonczony.");

            logger.info("------------------------------------------------------------------------------");
//			if(true) {
//                throw new ActivitiException("Dummy Task Error...");
//            }

            return integrationContext;
        };
    }

    @Bean("createContractAsDraftSrv")
    public Connector getCreateContractDraftConnector() {
        return integrationContext -> {
            logger.info("------------------------------------------------------------------------------");
            String var1 = this.getClass().getSimpleName()+" was called for instance " + integrationContext.getProcessInstanceId() + "[Business Key:" + integrationContext.getBusinessKey() + "] ClientName: " + integrationContext.getClientName() + ", ClientId: " + integrationContext.getClientId() + ", ProcessInstanceId: " +  integrationContext.getProcessInstanceId() + ", ParentProcessInstanceId: " + integrationContext.getParentProcessInstanceId();

            logger.info("[" + integrationContext.getId() + " : " + integrationContext.getProcessInstanceId() + " : " + integrationContext.getProcessDefinitionId() + " : " + integrationContext.getConnectorType() + "]");

            logger.info(var1);
            logger.info("Zmienne : " + integrationContext.getInBoundVariables());

            ContractData cData = integrationContext.getInBoundVariable("input_data");

            logger.info("Utworzenie draftu");

            logger.info("Zapisywanie stanu obiektu do DB...");
            logger.info("Proces zostal zakonczony.");

            logger.info("------------------------------------------------------------------------------");
//			if(true) {
//                throw new ActivitiException("Dummy Task Error...");
//            }

            return integrationContext;
        };
    }

    @Bean("find-proper-admin-connector")
    public Connector getFindAdminConnector() {
        return integrationContext -> {
            logger.info("------------------------------------------------------------------------------");
            String var1 = this.getClass().getSimpleName()+" was called for instance " + integrationContext.getProcessInstanceId() + "[Business Key:" + integrationContext.getBusinessKey() + "] ClientName: " + integrationContext.getClientName() + ", ClientId: " + integrationContext.getClientId() + ", ProcessInstanceId: " +  integrationContext.getProcessInstanceId() + ", ParentProcessInstanceId: " + integrationContext.getParentProcessInstanceId();

            logger.info("[" + integrationContext.getId() + " : " + integrationContext.getProcessInstanceId() + " : " + integrationContext.getProcessDefinitionId() + " : " + integrationContext.getConnectorType() + "]");

            logger.info(var1);
            logger.info("Zmienne : " + integrationContext.getInBoundVariables());

            // Ustalenie admina, na sztywno ;-)
            OrderServiceDTO order = integrationContext.getInBoundVariable("input_data");
            if(order == null || order.getServiceId() != 1) {
                logger.info("Admin uslugi: null");
                integrationContext.getOutBoundVariables().put("service_admin", null);
            }
            else {
                logger.info("Admin uslugi: admin");
                integrationContext.getOutBoundVariables().put("service_admin", "admin");
            }
//            integrationContext.addOutBoundVariable("CMD",
//                    "Invocation successful :-)");
//            integrationContext.getOutBoundVariables().put("zmienna_wejsciowa", "Dummy proces zakonczony ;-)");

            logger.info("------------------------------------------------------------------------------");
//			if(true) {
//                throw new ActivitiException("Dummy Task Error...");
//            }

            return integrationContext;
        };
    }

    @Bean("update-users-data")
    public Connector getUpdateUsersDataConnector() {
        return integrationContext -> {
            logger.info("------------------------------------------------------------------------------");
            String var1 = this.getClass().getSimpleName()+" was called for instance " + integrationContext.getProcessInstanceId() + "[Business Key:" + integrationContext.getBusinessKey() + "] ClientName: " + integrationContext.getClientName() + ", ClientId: " + integrationContext.getClientId() + ", ProcessInstanceId: " +  integrationContext.getProcessInstanceId() + ", ParentProcessInstanceId: " + integrationContext.getParentProcessInstanceId();

            logger.info("[" + integrationContext.getId() + " : " + integrationContext.getProcessInstanceId() + " : " + integrationContext.getProcessDefinitionId() + " : " + integrationContext.getConnectorType() + "]");

            logger.info(var1);
            logger.info("Zmienne : " + integrationContext.getInBoundVariables());

            OrderServiceDTO order = integrationContext.getInBoundVariable("input_data");

            logger.info("Zapisywanie stanu obiektu do DB...");
            logger.info("Proces zostal zakonczony.");

            logger.info("------------------------------------------------------------------------------");
//			if(true) {
//                throw new ActivitiException("Dummy Task Error...");
//            }

            return integrationContext;
        };
    }
    // ----------- Mechanizmy reagujace na zdarzenia wygenerowane przez silnik Activiti

    @Bean
    public TaskRuntimeEventListener<TaskAssignedEvent> taskAssignedListener() {
        return taskAssigned -> logger.info(">>> Task Assigned: '"
                + taskAssigned.getEntity().getName() +
                "' We can send a notification to the assginee: " + taskAssigned.getEntity().getAssignee());
    }

    @Bean
    public TaskRuntimeEventListener<TaskCompletedEvent> taskCompletedListener() {
        return taskCompleted -> logger.info(">>> Task Completed: '"
                + taskCompleted.getEntity().getName() +
                "' We can send a notification to the owner: " + taskCompleted.getEntity().getOwner());
    }

    @Bean
    public VariableEventListener<VariableCreatedEvent> variableCreatedEventListener() {
        return variableCreatedEvent -> variableCreatedEvents.add(variableCreatedEvent);
    }

    @Bean
    public ProcessRuntimeEventListener<ProcessCompletedEvent> processCompletedEventListener() {
        return processCompletedEvent -> processCompletedEvents.add(processCompletedEvent);
    }
}
