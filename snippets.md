# pl.devoxx.aggregatr.aggregation.IngredientsAggregator.fetchIngredients

List<ListenableFuture<Ingredient>> futures = ingredientsProperties
                .getListOfServiceNames(order)
                .stream()
                .map(this::harvest)
                .collect(Collectors.toList());
        ListenableFuture<List<Ingredient>> allDoneFuture = Futures.allAsList(futures);
        List<Ingredient> allIngredients = Futures.getUnchecked(allDoneFuture);
        allIngredients.stream()
                .filter((ingredient -> ingredient != null))
                .forEach(ingredientWarehouse::addIngredient);
        Ingredients ingredients = ingredientWarehouse.getCurrentState();
        return dojrzewatrUpdater.updateIfLimitReached(ingredients);        
        
# pl.devoxx.aggregatr.aggregation.IngredientsAggregator.harvest
        
        
return serviceRestClient.forExternalService()
                        .retryUsing(retryExecutor)
                        .get()
                        .withCircuitBreaker(withGroupKey(asKey(service)), () -> {
                            log.error("Can't connect to {}", service);
                            return null;
                        })
                        .onUrl(ingredientsProperties.getRootUrl() + "/" + service)
                        .andExecuteFor()
                        .anObject()
                        .ofTypeAsync(Ingredient.class);
                    
 
# microservice.json
                        
    "dojrzewatr" : {
                "path" : "pl/devoxx/dojrzewatr"
            }
         
                                    
# pl.devoxx.aggregatr.aggregation.DojrzewatrUpdater.notifyDojrzewatr 

                                    
serviceRestClient.forService("dojrzewatr")
                .retryUsing(retryExecutor)
                .post()
                .withCircuitBreaker(withGroupKey(asKey("dojrzewatr_notification")))
                .onUrl("/brew")
                .body(ingredients)
                .withHeaders().contentType(Version.DOJRZEWATR_V1)
                .andExecuteFor()
                .ignoringResponseAsync();                                    