package de.tuberlin.vsp.dataScience;

import org.matsim.api.core.v01.TransportMode;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.BarTrace;
import tech.tablesaw.plotly.traces.ScatterTrace;
import tech.tablesaw.plotly.traces.Trace;

import java.util.ArrayList;
import java.util.List;

class Modestats {

	public static void main( String[] args ){

		String filename = "../public-svn/matsim/tutorial/datascience2024/matsim_outputs/output-1pct/base/berlin-v6.3.modestats.csv";

		Table table = Table.read().usingOptions( CsvReadOptions.builder( filename ).separator( ';' ).build() );

		System.out.println( table.print() );

		Layout layout = Layout.builder().barMode( Layout.BarMode.STACK ).build();

		List<Trace> traces = new ArrayList<>();
		traces.add( getBarTrace( table, TransportMode.car ) );
		traces.add( getBarTrace( table, TransportMode.bike ) );
		traces.add( getBarTrace( table, "freight" ) );
		traces.add( getBarTrace( table, TransportMode.pt ) );
		traces.add( getBarTrace( table, TransportMode.ride ) );
		traces.add( getBarTrace( table, TransportMode.walk ) );

		Figure figure = new Figure( layout, traces.toArray(traces.toArray( new Trace[0] ) ) );
		Plot.show( figure );



	}
	private static BarTrace getBarTrace( Table table, String mode ){
		BarTrace trace = BarTrace.builder( table.intColumn( "iteration" ), table.numberColumn( mode ) ).name( mode ).build();
		return trace;
	}

}
