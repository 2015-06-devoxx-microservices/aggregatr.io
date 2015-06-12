io.codearte.accurest.dsl.GroovyDsl.make {
    request {
        method 'GET'
        url '/ingredients'
        headers {
            header 'Content-Type': 'application/vnd.pl.devoxx.aggregatr.v1+json'
        }
    }
    response {
        status 200
        body(
            ingredients: [
                    [type: 'MALT', quantity: 100],
                    [type: 'WATER', quantity: 200],
                    [type: 'HOP', quantity: 300],
                    [type: 'YIEST', quantity: 400]
            ]
        )
    }
}