package de.tuberlin.vsp.dataScience;

import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.joining.DataFrameJoiner;

class Join{
//	private static final String baseDir = "../public-svn/matsim/tutorial/datascience2024/matsim_outputs/output-1pct/base/berlin-v6.3.";
	private static final String baseDir = "../public-svn/matsim/scenarios/countries/de/leipzig/projects/namav/v1.3.1/base-case/leipzig-10pct.";

	public static void main( String[] args ){

//		String filename = baseDir + "output_activities.csv";
//		Table acts = Table.read().usingOptions( CsvReadOptions.builder( filename ).separator( ';' ).build() );
//		System.out.println("=== acts:");
//		System.out.println( acts.print() );
//		System.out.println( System.lineSeparator() );
//
//		String filename2 = baseDir + "output_persons.csv";
//		Table persons = Table.read().usingOptions( CsvReadOptions.builder( filename2 ).separator( ';' ).build() );
//		System.out.println("=== acts:");
//		System.out.println( persons.print() );
//		System.out.println( System.lineSeparator() );

		String filename3 = baseDir + "output_legs.csv";
		Table legs = Table.read().usingOptions( CsvReadOptions.builder( filename3 ).separator( ';' ).build() );
		System.out.println( "=== legs:" );
		System.out.println( legs.print() );
		System.out.println( System.lineSeparator() );

//		String filename4 = baseDir + "output_trips.csv";
//		Table trips = Table.read().usingOptions( CsvReadOptions.builder( filename4 ).separator( ';' ).build() );
//		System.out.println( "=== trips:" );
//		System.out.println( trips.print() );
//		System.out.println( System.lineSeparator() );
//
//		System.out.println( "=== joined:" );
//		Table result = new DataFrameJoiner( acts, "person" ).fullOuter( true, acts );
//		System.out.println( result.print() );
//		System.out.println( System.lineSeparator() );

	}

}
