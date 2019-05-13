package com.camunda.consulting.example_testshowcase;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class LargerProcessTest {

  @ClassRule
  @Rule
  public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

  private static final String PROCESS_DEFINITION_KEY = "LargerProcess";

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  /**
   * Just tests if the process definition is deployable.
   */
  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testParsingAndDeployment() {
    // nothing is done here, as we just want to check for exceptions during deployment
  }

  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testHappyPath() {
	  ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
	  
	  // Now: Drive the process by API and assert correct behavior by camunda-bpm-assert
	  assertThat(processInstance).isWaitingAt("projectRegistration").task().hasName("task1");
	  complete(task());
	  assertThat(processInstance).isWaitingAt("isSubjectToClauseT").task().hasName("task2");
	  complete(task(), withVariables("prc_is_subject_to_clause_t", true));
	  assertThat(processInstance).isWaitingAt("issueApprovalCode").task().hasName("task4");
	  complete(task());
	  assertThat(processInstance).isEnded();
  }
  
  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testCompleteIsSubjectToClauseTWithFalse() {
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
        .startBeforeActivity("isSubjectToClauseT")
        .execute();
    
    assertThat(processInstance).isWaitingAt("isSubjectToClauseT");
    complete(task(), withVariables("prc_is_subject_to_clause_t", false));
    
    assertThat(processInstance).isWaitingAt("isEnactedByStateCouncil").task().hasName("task3");
  }
  
  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testCompleteIsEnactedByStateCouncilWithTrue() {
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
        .startBeforeActivity("isEnactedByStateCouncil")
        .setVariables(withVariables("prc_is_subject_to_clause_t", false))
        .execute();
    
    assertThat(processInstance).isWaitingAt("isEnactedByStateCouncil");
    
    complete(task(), withVariables("prc_is_enacted_by_state_council", true));
    assertThat(processInstance).isWaitingAt("confirmationAndCostAcception").task().hasName("task6");
  }
  
  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testCompleteIsEnactedByStateCouncilWithFalse() {
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
        .startBeforeActivity("isEnactedByStateCouncil")
        .setVariables(withVariables("prc_is_subject_to_clause_t", false))
        .execute();
    
    assertThat(processInstance).isWaitingAt("isEnactedByStateCouncil");
    
    complete(task(), withVariables("prc_is_enacted_by_state_council", false));
    assertThat(processInstance).isWaitingAt("requiredToEditOrSelectArbiters").task().hasName("task7");
  }

  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testCompleteConfirmationAndCostAcceptionWithOption1() {
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
        .startBeforeActivity("confirmationAndCostAcception")
        .setVariables(withVariables(
            "prc_is_subject_to_clause_t", false, 
            "prc_is_enacted_by_state_council", true))
        .execute();
    
    assertThat(processInstance).isWaitingAt("confirmationAndCostAcception");
    
    complete(task(), withVariables(
        "prc_confirmation", true, 
        "prc_is_cost_accepted", true,
        "prc_required_to_edit", false));
    assertThat(processInstance).isWaitingAt("issueApprovalCode");
  }

  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testCompleteConfirmationAndCostAcceptionWithOption2() {
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
        .startBeforeActivity("confirmationAndCostAcception")
        .setVariables(withVariables(
            "prc_is_subject_to_clause_t", false, 
            "prc_is_enacted_by_state_council", true))
        .execute();
    
    assertThat(processInstance).isWaitingAt("confirmationAndCostAcception");
    
    complete(task(), withVariables(
        "prc_confirmation", false, 
        "prc_is_cost_accepted", true,
        "prc_required_to_edit", false));
    assertThat(processInstance).isWaitingAt("disapprovalNotifying").task().hasName("task5");
    
    complete(task());
    assertThat(processInstance).isEnded();
  }

  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testCompleteConfirmationAndCostAcceptionWithOption3() {
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
        .startBeforeActivity("confirmationAndCostAcception")
        .setVariables(withVariables(
            "prc_is_subject_to_clause_t", false, 
            "prc_is_enacted_by_state_council", true))
        .execute();
    
    assertThat(processInstance).isWaitingAt("confirmationAndCostAcception");
    
    complete(task(), withVariables(
        "prc_confirmation", true, 
        "prc_is_cost_accepted", false,
        "prc_required_to_edit", false));
    assertThat(processInstance).isWaitingAt("requiredToEditOrSelectArbiters");
  }
  
  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testCompleteConfirmationAndCostAcceptionWithOption4() {
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
        .startBeforeActivity("confirmationAndCostAcception")
        .setVariables(withVariables(
            "prc_is_subject_to_clause_t", false, 
            "prc_is_enacted_by_state_council", true))
        .execute();
    
    assertThat(processInstance).isWaitingAt("confirmationAndCostAcception");
    
    complete(task(), withVariables(
        "prc_confirmation", true, 
        "prc_required_to_edit", true));
    assertThat(processInstance).isWaitingAt("projectRegistration");
  }
  
  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testCompleteRequiredToEditOrSelectArbitersWithTrue() {
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
        .startBeforeActivity("requiredToEditOrSelectArbiters")
        .setVariables(withVariables(
            "prc_is_subject_to_clause_t", false, 
            "prc_is_enacted_by_state_council", false))
        .execute();
    
    assertThat(processInstance).isWaitingAt("requiredToEditOrSelectArbiters");
    
    complete(task(), withVariables("prc_required_to_edit_or_select_arbiters", true));
    assertThat(processInstance).isWaitingAt("projectRegistration");
  }

  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testCompleteRequiredToEditOrSelectArbitersWithFalse() {
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
        .startBeforeActivity("requiredToEditOrSelectArbiters")
        .setVariables(withVariables(
            "prc_is_subject_to_clause_t", false, 
            "prc_is_enacted_by_state_council", false))
        .execute();
    
    assertThat(processInstance).isWaitingAt("requiredToEditOrSelectArbiters");
    
    complete(task(), withVariables("prc_required_to_edit_or_select_arbiters", false));
    assertThat(processInstance).isWaitingAt("callForArbitration").task().hasName("task9");
    
    complete(task());
    
    assertThat(processInstance).isWaitingAt("isProjectApproved").task().hasName("task8");
  }
  
  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testCompleteIsProjectApprovedWithTrue() {
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
        .startBeforeActivity("isProjectApproved")
        .setVariables(withVariables(
            "prc_is_subject_to_clause_t", false, 
            "prc_is_enacted_by_state_council", false, 
            "prc_required_to_edit_or_select_arbiters", false))
        .execute();
    
    assertThat(processInstance).isWaitingAt("isProjectApproved");
    
    complete(task(), withVariables("prc_is_project_approved", true));
    assertThat(processInstance).isEnded();
  }
  
  @Test
  @Deployment(resources = "largerProcess.bpmn")
  public void testCompleteIsProjectApprovedWithFalse() {
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
        .startBeforeActivity("isProjectApproved")
        .setVariables(withVariables(
            "prc_is_subject_to_clause_t", false, 
            "prc_is_enacted_by_state_council", false, 
            "prc_required_to_edit_or_select_arbiters", false))
        .execute();
    
    assertThat(processInstance).isWaitingAt("isProjectApproved");
    
    complete(task(), withVariables("prc_is_project_approved", false));
    assertThat(processInstance).isWaitingAt("disapprovalNotifying");
  }  
}
