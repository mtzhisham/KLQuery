package dev.moataz.klquery.sample

import org.junit.Test

import org.junit.Assert.*

class QueryUnitTest {
    @Test
    fun query_isEqual() {
        val query = charactersQuery {
            resultsQuery {
                id
                name
            }
        }.buildQueryString()

       assertEquals(query, "{\"query\":\"query {\\r\\n charactersQuery {\\r\\n results {\\r\\n id\\r\\n name\\r\\n  }\\r\\n  }\\r\\n }\\r\\n  \"}")
    }
}