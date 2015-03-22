import org.junit.Test
import org.junit.runner.RunWith

class PortalPage extends Page
{
    static url = "http://example.com"
    static at = { title == "Local Wiki System" }
    static content = {
        field { $("input", name: "q") }
    }
}

class ResultsPage extends Page
{
    static at = { title.endsWith "Wiki Search"}
    static content = {
        results { $("li.g") }
        result { i -> results[i] }
        resultLink { i -> result(i).find("a.l") }
        firstResultLink { resultLink(0) }
    }
}

class WhenSearching extends GebReportingTest
{
    @Test
    void "should show Geb page"(){
        to PortalPage
        assert at(PortalPage)
        page.field.value("Groovy Geb")
        waitFor { at ResultsPage}
    }
}

@RunWith(GebFSMRunner)
class UseFSMBasedTest
{
    def eddges = [
            Portal >> Search "" >> Portal,
            Portal >> Search "Groovy Geb" >> SearchResult,
            SearchResult >> Search "" >> Portal,
            SearchResult >> Search "JUnit Geb" >> SearchResult,
    ]
}