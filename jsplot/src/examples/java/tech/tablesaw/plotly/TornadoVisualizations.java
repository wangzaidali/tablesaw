/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.tablesaw.plotly;

import tech.tablesaw.AbstractExample;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.NumberColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.api.BarPlot;
import tech.tablesaw.plotly.api.BoxPlot;
import tech.tablesaw.plotly.api.Histogram;
import tech.tablesaw.plotly.api.ParetoPlot;
import tech.tablesaw.plotly.api.PiePlot;
import tech.tablesaw.selection.Selection;

import static tech.tablesaw.aggregate.AggregateFunctions.*;

/**
 * Usage example using a Tornado data set
 */
public class TornadoVisualizations extends AbstractExample {

    public static void main(String[] args) throws Exception {

        Table tornadoes = Table.read().csv("../data/tornadoes_1950-2014.csv");
        assert (tornadoes != null);

        out(tornadoes.structure());
        tornadoes.setName("tornadoes");

        // filter out some bad data point
        tornadoes = tornadoes.where(tornadoes.numberColumn("Start Lat").isGreaterThan(20f));
        NumberColumn scale = tornadoes.numberColumn("scale");

        scale.set(scale.isEqualTo(-9), DoubleColumn.MISSING_VALUE);

        Table fatalities1 = tornadoes.summarize("fatalities", sum).by("scale");

        BarPlot.showHorizontal(
                "fatalities by scale",
                fatalities1,
                "scale",
                "sum [fatalities]");

        PiePlot.show("fatalities by scale", fatalities1, "scale", "sum [fatalities]");

        Table fatalities2 = tornadoes.summarize("fatalities", sum).by("state");

        ParetoPlot.showVertical(
                "Tornado Fatalities by State",
                fatalities2,
                "state",
                "sum [fatalities]");

        Table injuries1 = tornadoes.summarize("injuries", mean).by("scale");
        BarPlot.showHorizontal("Tornado Injuries by Scale", injuries1, "scale", "mean [injuries]");
        out(injuries1);

        // distributions
        Table level5 = tornadoes.where(scale.isEqualTo(5));

        Histogram.show("Distribution of injuries for Level 5", level5, "injuries");

        BoxPlot.show("Average number of tornado injuries by scale", tornadoes,"scale", "injuries");

        out();
        out("Extract month from the date and make it a separate column");
        StringColumn month = tornadoes.dateColumn("Date").month();
        out(month.summary());

        out("Add the month column to the table");
        tornadoes.insertColumn(2, month);
        out(tornadoes.columnNames());

        //TODO(lwhite): Provide a param for title of the new table (or auto-generate a better one).
        Table injuriesByScale = tornadoes.summarize("Injuries", median).by("Scale");
        Table fob = tornadoes.summarize("Injuries", min).by("Scale", "State");
        out(fob);
        injuriesByScale.setName("Median injuries by Tornado Scale");
        out(injuriesByScale);

        injuriesByScale = tornadoes.summarize("Injuries", mean).by("Scale");
        injuriesByScale.setName("Average injuries by Tornado Scale");
        out(injuriesByScale);

        //TODO(lwhite): Provide a param for title of the new table (or auto-generate a better one).
        Table injuriesByScaleState = tornadoes.summarize("Injuries", median).by("Scale", "State");
        injuriesByScaleState.setName("Median injuries by Tornado Scale and State");
        out(injuriesByScaleState);

        Table injuriesByScaleState2 = tornadoes.summarize("Injuries", sum).by("State", "Scale");
        injuriesByScaleState2.setName("Total injuries by Tornado Scale and State");
        out(injuriesByScaleState2);

        // Average days between tornadoes in the summer

        // alternate, somewhat more precise approach
        DateColumn date = tornadoes.dateColumn("Date");

        Selection summerFilter =
                  date.month().isIn("JULY", "AUGUST")
                        .or(date.month().isEqualTo("JUNE")
                            .and(date.dayOfMonth().isGreaterThanOrEqualTo(21)))
                        .or(date.month().isEqualTo("SEPTEMBER")
                            .and(date.dayOfMonth().isLessThanOrEqualTo(22)));

        //Table summer = tornadoes.select(selection);
        Table summer = tornadoes.where(summerFilter);
        summer = summer.sortAscendingOn("Date", "Time");
        summer.addColumns(summer.dateColumn("Date").lag(1));

        // calculate the difference between a date and the prior date using the lagged column
        DateColumn summerDate = summer.dateColumn("Date");
        DateColumn laggedDate = summer.dateColumn("Date lag(1)");
        NumberColumn delta = laggedDate.daysUntil(summerDate);  // the lagged date is earlier
        summer.addColumns(delta);

        out(summer.first(4));

        // now we can summarize by year so we don't inadvertently include differences between multiple years
        Table summary = summer.summarize(delta, mean, countNonMissing).by(summerDate.year());
        out(summary);

        // taking the mean of the annual means gives us an approximate answer
        // we could also use the count value calculated above to get a weighted average
        out(summary.nCol(1).mean());

        out();

        out("Writing the revised table to a new csv file");
        tornadoes.write().csv("../data/rev_tornadoes_1950-2014.csv");
    }
}