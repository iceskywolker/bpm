package com.pracelab.demo.dto;

import javax.validation.constraints.NotNull;

public class UserStep {
    @NotNull
    private String stepName;
    @NotNull
    private String stepId;

    private String jsonSchema;

    private String uiSchema;

    private String formData;

    private String responseUrl;

    public UserStep(@NotNull String stepId, @NotNull String stepName, String jsonSchema, String uiSchema, String formData, String responseUrl) {
        this.stepId = stepId;
        this.stepName = stepName;
        this.jsonSchema = jsonSchema;
        this.uiSchema = uiSchema;
        this.formData = formData;
        this.responseUrl = responseUrl;
    }

    public String getStepId() {
        return stepId;
    }

    public String getStepName() {
        return stepName;
    }

    public String getJsonSchema() {
        return jsonSchema;
    }

    public String getUiSchema() {
        return uiSchema;
    }

    public String getFormData() {
        return formData;
    }

    public String getResponseUrl() {
        return responseUrl;
    }

    /*
    jsonSchema - specyfikacja danych wymaganych od użytkownka do popchnięcia tego kroku (na tej podstawie generowany będzie dla użytkownika formularz przy uzyciu biblioteki https://github.com/rjsf-team/react-jsonschema-form)
uiSchema - dodatkowe dane potrzebne do wysterowania wyglądu i zachowania tego formularza (format narzucony przez powyższą bibliotekę)
formData - opcjonalne pole zawierające bieżący stan wypełnienia formularza (przydatne np. w przypadku edytowania jakich istniejących już danych)
responseUrl - adres na jaki UI ma odesłać formData (po wypełnieniu formularza przez użytkownika) w celu popchnięcia kroku procesu
     */
}
