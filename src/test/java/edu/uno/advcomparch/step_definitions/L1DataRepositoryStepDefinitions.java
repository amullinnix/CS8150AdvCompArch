package edu.uno.advcomparch.step_definitions;

import edu.uno.advcomparch.repository.DataRepository;
import edu.uno.advcomparch.repository.DataResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class L1DataRepositoryStepDefinitions {

    private DataRepository<String, Integer> dataRepository;
    private DataResponse actualData;

    @Given("Data is {string}")
    public void data_is(String stringData) {
        dataRepository = new DataRepository<>();
        dataRepository.write(stringData);
    }

    @When("I ask for data from address {integer}")
    public void i_ask_for_data(Integer address) {
        actualData = dataRepository.get(address);
    }

    @Then("I should be told {string}")
    public void i_should_be_told(String expectedData) {
        assertEquals(expectedData, actualData.getData());
    }
}
