io.codearte.accurest.dsl.GroovyDsl.make {
    request {
        method 'POST'
        url '/ingredients'
        headers {
            header 'Content-Type': 'application/vnd.pl.devoxx.aggregatr.v1+json'
        }
        body('''
            { "items" : ["MALT"] }
        ''')
    }
    response {
        status 200
        body(
            ingredients: [
                    [type: 'MALT', quantity: 200]
            ]
        )
    }
}