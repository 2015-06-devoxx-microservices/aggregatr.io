package com.ofg.twitter.place.extractor

import com.ofg.twitter.place.extractor.Place.PlaceDetails
import com.ofg.twitter.place.extractor.PlaceExtractor.PlaceResolutionProbability
import com.ofg.twitter.place.extractor.metrics.MatchProbabilityMetrics
import com.ofg.twitter.place.model.Tweet
import groovy.transform.PackageScope

@PackageScope
class CoordinatesPlaceExtractor implements PlaceExtractor {

    public static final String PLACE_EXTRACTION_NAME = 'twitter_coordinates_section'

    private final CityFinder cityFinder
    private final MatchProbabilityMetrics metrics

    public CoordinatesPlaceExtractor(CityFinder cityFinder, MatchProbabilityMetrics matchProbabilityMetrics) {
        this.cityFinder = cityFinder
        this.metrics = matchProbabilityMetrics
    }

    @Override
    Optional<Place> extractPlaceFrom(Tweet parsedTweet) {
        if (parsedTweet.coordinates == null) {
            return Optional.empty()
        }
        def(double longitude, double latitude) = parsedTweet.coordinates.coordinates
        Optional<PlaceDetails> placeDetails = cityFinder.findCityFromCoordinates(longitude, latitude)
        return placeIfPresentOrEmptyOptional(placeDetails)
    }

    private Optional<Place> placeIfPresentOrEmptyOptional(Optional<PlaceDetails> placeDetails) {
        if (placeDetails.isPresent()) {
            metrics.update(placeResolutionProbability)
            return Optional.of(new Place(placeDetails.get(), origin, placeResolutionProbability))
        } else {
            return Optional.empty()
        }
    }

    @Override
    String getOrigin() {
        return PLACE_EXTRACTION_NAME
    }

    @Override
    PlaceResolutionProbability getPlaceResolutionProbability() {
        return PlaceResolutionProbability.HIGH
    }
}
