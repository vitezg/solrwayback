/**
 * Transforms an array of harvest dates as epoch times into a suitable format for rendering a graph with iteration.
 */
function groupHarvestDatesByYearAndMonth(harvestDates) {

    const maxDate = new Date(_.max(harvestDates));
    const minDate = new Date(_.min(harvestDates));

    // Parse the harvest dates into an array of Date objects.
    // Validate that the dates are integers.
    const parsedHarvestDates = harvestDates
        .filter(date => parseInt(date) !== NaN)
        .map(date => new Date(date));

    // Build an object with keys as the years.
    const yearRangeObject = buildYearRangeObject(minDate, maxDate);

    // Build Harvest Data Object.
    let harvestDataObject = addActivityLevelToDataObject(
        buildHarvestDataObject(yearRangeObject, parsedHarvestDates)
    )

    return {
        minDate: minDate,
        maxDate: maxDate,
        dates: harvestDataObject,
        numberOfHarvests: harvestDates.length
    }
}


/**
 * Build an object with keys as the years, e.g.
 * {
 *     2007: [],
 *     2008: [],
 *     ...
 * }
 */
function buildYearRangeObject(minDate, maxDate) {
    const yearRangeArray = buildYearRangeArray(minDate, maxDate);
    const yearRangeObject = {};

    for (year of yearRangeArray) {
        yearRangeObject[year] = [];
    }  

    return yearRangeObject;
}


/**
 * Build an array of years from the minDate year to the maxDate year. E.g. [2007, 2008, 2009, 2010, 2011, ..., 2017]
 * minDate, maxDate are Date instances
 */
function buildYearRangeArray(minDate, maxDate) {
    return yearRangeArray = [...Array(maxDate.getFullYear() - minDate.getFullYear() + 1).keys()]     // e.g. [0, 1, 2, ..., 10]
        .map(year => year + minDate.getFullYear());                                                  // e.g. [2007, 2008, 2009, ..., 2017]
}


/**
 * Add months to the year range object.
 * Output is:
 * {
 *     2007: {
 *         1: {
 *             dates: [ ... ],
               numberOfHarvests: 5023
 *         },
 *         ...
 *     },
 *     ...
 * }
 */
function buildHarvestDataObject(yearRangeObject, parsedHarvestDates) {

    const arrayOfMonthValues = [...Array(12).keys()];       // [0, 1, 2, ..., 11]

    // Iterate over all years in the yearRangeObject
    for (let yearAsString of Object.keys(yearRangeObject)) {
        const year = parseInt(yearAsString);        // Since Object.keys() returns an array of strings, we need to convert years to a number.

        // Iterate over all months (0-11)
        for (month of arrayOfMonthValues) {
            const allHarvestDatesInMonth = getHarvestsForMonth(year, month, parsedHarvestDates);

            yearRangeObject[year][month] = {
                dates: allHarvestDatesInMonth,
                numberOfHarvests: allHarvestDatesInMonth.length
            }
        }
    }

    return yearRangeObject;
}


/**
 * Return an array of all the parsedHarvestDates in the given year and month.
 */
function getHarvestsForMonth(year, month, parsedHarvestDates) {
    return parsedHarvestDates
        .filter(date => date.getMonth() === month && date.getFullYear() === year)
}

/**
 * 
 */
function addActivityLevelToDataObject(harvestDataObject) {

    // TODO!
    return harvestDataObject;
}


let harvestDateComponent = Vue.component('harvest-date', {
    props: ['message'],
    data: () => {
        return {
            harvestData: null,
        }
    },
    template: `
        <div v-if="harvestData">
            <p>Harvests: {{ harvestData.numberOfHarvests }}</p>
            <table  v-for="(months, year) in harvestData.dates">
                <thead>
                    <tr>
                        <th>{{ year }}</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(data, month) in months">
                        <td>{{ month }}: {{ data.numberOfHarvests }}</td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div v-else>
            <p>Fetching harvests</p>
        </div>
    `,
    created() {
        this.$http.get("/solrwayback/services/harvestDates?url=" + encodeURIComponent(window.solrWaybackConfig.url))
        .then(response => {
            this.harvestData = groupHarvestDatesByYearAndMonth(response.data.dates);
        });
    }
});


let app = new Vue({
    el: "#app"
});