package de.tuberlin.vsp.dataScience;

import tech.tablesaw.aggregate.AggregateFunctions;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.BarTrace;
import tech.tablesaw.plotly.traces.Trace;

import java.util.ArrayList;
import java.util.List;

class Activities{

	public static void main( String[] args ){

		String filename = "../public-svn/matsim/tutorial/datascience2024/matsim_outputs/output-1pct/base/berlin-v6.3.output_activities.csv";

		Table tt = Table.read().usingOptions( CsvReadOptions.builder( filename ).separator( ';' ).build() );

		System.out.println( tt.print() );

		for( Column<?> column : tt.columns() ){
			System.out.println( "name=" + column.name() + "; type=" + column.type() );
		}

		Table p1 = tt.summarize( "activity_type", AggregateFunctions.count ).by( "activity_type");
		System.out.println( p1 );

		System.exit(-1);



		final String DIFFERENCE_DISTANCE = "difference_distance";
		final String MAIN_MODE = "main_mode";

		tt.addColumns( tt.numberColumn( "traveled_distance" ).subtract( tt.numberColumn( "euclidean_distance" ) ).setName( DIFFERENCE_DISTANCE ) );
//		tt.removeColumns( "dep_time", "trav_time", "wait_time" );
//		Table p2 = tt.where( tt.numberColumn( DIFFERENCE_DISTANCE ).isGreaterThan( 1000 ) );
//		Table p3 = p2.sortAscendingOn( "dep_time" );

		Table p4 = tt.summarize( "traveled_distance", AggregateFunctions.mean, AggregateFunctions.median ). by( "main_mode").sortAscendingOn( "main_mode" );

		System.out.println( p4.print() );



//		Table rr = tt.summarize( MAIN_MODE, AggregateFunctions.count ).by( MAIN_MODE);
		Table rr = tt.summarize( MAIN_MODE, AggregateFunctions.count ).apply();

		System.out.println( rr.print() );

		System.exit(-1);

		Layout layout = Layout.builder().barMode( Layout.BarMode.STACK ).build();

		List<Trace> traces = new ArrayList<>();
		{
			BarTrace.BarBuilder builder = BarTrace.builder( rr.stringColumn( MAIN_MODE ), rr.numberColumn( "Count [" + MAIN_MODE + "]") );
			builder.orientation( BarTrace.Orientation.HORIZONTAL );
			traces.add( builder.build() );
		}
//		{
//			final String mode = TransportMode.car;
//			ScatterTrace.ScatterBuilder builder = ScatterTrace.builder( tt.numberColumn( "iteration" ), tt.numberColumn( mode ) )
//									  .name( mode )
//									  .mode( ScatterTrace.Mode.MARKERS );
//			traces.add( builder.build() );
//		}
//		{
//			final String mode = TransportMode.bike;
//			ScatterTrace.ScatterBuilder builder = ScatterTrace.builder( tt.numberColumn( "iteration" ), tt.numberColumn( mode ) )
//									  .name( mode )
//									  .mode( ScatterTrace.Mode.MARKERS );
//			traces.add( builder.build() );
//		}
		Figure figure = new Figure( layout, traces.toArray(traces.toArray( new Trace[0] ) ) );
		Plot.show( figure );



	}

}
