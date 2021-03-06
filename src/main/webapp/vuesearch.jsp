<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">

    <title>SolrWayback Search</title>
    <link rel="stylesheet" type="text/css" media="all" href="./css/solrwayback.css">

    <link rel="stylesheet" href="js/leaflet/leaflet.css" />  
    <script type="text/javascript" src="js/leaflet/leaflet.js"></script>

    <script type="text/javascript" src="js/jquery-3.2.1.min.js"></script>

    <script type="text/javascript" src="js/vue.js"></script>
    <script type="text/javascript" src="js/vue-resource.min.js"></script>
    <script type="text/javascript" src="js/vue-router.js"></script>




</head>
<body>
    <div class="wrapper" id="app">
        <h1>SolrWayback Search</h1>

        <search-box :setup-search="setupSearch" :my-query="myQuery" :image-search="imageSearch"  :url-search="urlSearch"
                    :image-geo-search="imageGeoSearch" :clear-search="clearSearch":grouping="grouping" :set-grouping="setGrouping"></search-box>

        <map-box v-if="imageGeoSearch" :marker-position="markerPosition" :place-marker="placeMarker" :do-search="doSearch"
                 :total-hits="totalHits"></map-box>


        <selected-facets-box v-if="facetFields.length > 0 && !imageSearch" :facet-fields="facetFields" :setup-search="setupSearch"
                             :my-query="myQuery" :clear-facets="clearFacets"></selected-facets-box>

        <error-box v-if="errorMsg" :error-msg="errorMsg" :my-query="myQuery"></error-box>


        <facet-box v-if="totalHits > 0 && myQuery && !errorMsg && !imageSearch" :my-facets="myFacets" :my-query="myQuery"
                   :setup-search="setupSearch"></facet-box>


        <zerohits-box v-if="totalHits == 0 && searchResult && myQuery != '' && myQuery && !spinner" :my-query="myQuery" :image-search="imageSearch"></zerohits-box>

        <div class="result" v-if="myQuery && !errorMsg && !imageSearch">

            <pager-box v-if="searchResult && !spinner" :setup-search="setupSearch" :total-hits="totalHits" :total-hits-duplicates="totalHitsDuplicates" :start="start" :my-query="myQuery"
                       :filters="filters" :image-search="imageSearch" :grouping="grouping"></pager-box>

            <result-box v-if="totalHits > 0" :search-result="searchResult" :fullpost="fullpost" :image-objects="imageObjects" :base-url="baseUrl"
                        :setup-search="setupSearch" :clear-facets="clearFacets" :get-fullpost="getFullpost" :openbase-url="openbaseUrl"></result-box>


            <pager-box  v-if="totalHits > 21 && !spinner" :setup-search="setupSearch" :total-hits="totalHits" :total-hits-duplicates="totalHitsDuplicates"
                        :start="start" is-bottom="true"></pager-box>
        </div>

        <div class="result images" v-if="myQuery && !errorMsg && imageSearch">

            <pager-box v-if="searchResult" :setup-search="setupSearch" :total-hits="totalHits" :start="start" :my-query="myQuery"
                       :filters="filters" :show-spinner="showSpinner"
                       :hide-spinner="hideSpinner" :image-search="imageSearch"></pager-box>

            <result-box-images :search-result="searchResult" :setup-search="setupSearch" :clear-facets="clearFacets"></result-box-images>

        </div>

        <div v-if="spinner" id="overlay"></div>
        <div v-if="spinner" id="spinnerVue">Searching...</div>

    </div>
<!-- This include must be at bottom -->
    <script type="text/javascript" charset="utf-8" src="js/app.js"></script>
</body>
</html>