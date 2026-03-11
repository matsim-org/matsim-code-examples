package org.matsim.codeexamples.network;

import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.osm.networkReader.LinkProperties;
import org.matsim.contrib.osm.networkReader.OsmTags;
import org.matsim.contrib.osm.networkReader.SupersonicOsmNetworkReader;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.utils.gis.shp2matsim.ShpGeometryUtils;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Example on how to convert osm data from e.g. http://download.geofabrik.de into a MATSim network. This examle puts all
 * motorways and primary roads into the MATSim network. If a link is contained in the supplied shape, also minor and
 * residential raods are put into the MATsim network.
 * <p>
 * After parsing the OSM-data, unreachable areas of the network are removed by using the network cleaner
 */
public class RunCreateNetworkFromOSM {

	private static String UTM32nAsEpsg = "EPSG:32649";
	private static Path input = Paths.get("E:/MATsim/MatSimShuJu/panyui.osm.pbf");
	private static Path filterShape = Paths.get("E:/mike urban/数据下载/广州市1：25/boua.shp");

	public static void main(String[] args) throws MalformedURLException {
		new RunCreateNetworkFromOSM().create();
	}

	private void create() throws MalformedURLException {

		// choose an appropriate coordinate transformation. OSM Data is in WGS84. When working in central Germany,
		// EPSG:25832 or EPSG:25833 as target system is a good choice
		CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation(
				TransformationFactory.WGS84, UTM32nAsEpsg
		);

		// load the geometries of the shape file, so they can be used as a filter during network creation
		// using PreparedGeometry instead of Geometry increases speed a lot (usually)
		List<PreparedGeometry> filterGeometries = ShpGeometryUtils.loadPreparedGeometries(filterShape.toUri().toURL());

		// create an osm network reader with a filter
		SupersonicOsmNetworkReader reader = new SupersonicOsmNetworkReader.Builder()
				.setCoordinateTransformation(transformation)
				.setIncludeLinkAtCoordWithHierarchy((coord, hierarchyLevel) -> {

					// take all links which are motorway, trunk, or primary-street regardless of their location
					if (hierarchyLevel <= LinkProperties.LEVEL_RESIDENTIAL) return true;//这段代码是控制保留区域外的道路等级代码

					// whithin the shape, take all links which are contained in the osm-file
					return ShpGeometryUtils.isCoordInPreparedGeometries(coord, filterGeometries);//这段代码和下面的代码二选一，此代码是控制保留区域外的道路等级代码
//                    boolean isInPanyu = ShpGeometryUtils.isCoordInPreparedGeometries(coord, filterGeometries);//这一部分是控制保留区域内的道路等级代码
//                    if (isInPanyu) {
//                        // 规则 3：【区域内低等级道路剔除】
//                        // 虽然在番禺区内，但如果是小区道路(RESIDENTIAL)、服务道(SERVICE)等，统统扔掉！
//                        // 这里设置 <= LEVEL_TERTIARY，意思是只保留“支路(TERTIARY)”及以上级别的好路。
//                        return hierarchyLevel <= LinkProperties.LEVEL_TERTIARY;
//                    } else {
//                        // 规则 4：【区域外杂鱼剔除】
//                        // 既不是主干道，又不在番禺区内的小路，毫不留情地删掉。
//                        return false;
//                    }
                    //
				})
				.setAfterLinkCreated((link, osmTags, direction) -> {

					// if the original osm-link contains a cycleway tag, add bicycle as allowed transport mode
					// although for serious bicycle networks use OsmBicycleNetworkReader
					if (osmTags.containsKey(OsmTags.CYCLEWAY)) {
						Set<String> modes = new HashSet<>(link.getAllowedModes());
						modes.add(TransportMode.bike);
						link.setAllowedModes(modes);
					}
                    String highwayType = osmTags.get("highway");

                    if (highwayType != null) {
                        // 如果是主干道 (primary)，且它原本被识别成只有 1 条车道
                        if (highwayType.equals("primary") && link.getNumberOfLanes() <= 1.0) {
                            link.setNumberOfLanes(3.0);       // 强行改成 3 车道
                            link.setCapacity(4500.0);         // 强行扩容为 4500 辆/小时
                            link.setFreespeed(60.0 / 3.6);    // 限速强行设为 60 km/h
                        } else if (highwayType.equals("primary")) {
                            link.setFreespeed(60.0 / 3.6);//强制设置primary的限速
                        }
                        // 如果是快速路或高速 (motorway/trunk)，强行改成 3 车道
                        else if ((highwayType.equals("motorway") || highwayType.equals("trunk")) && link.getNumberOfLanes() <= 2.0) {
                            link.setNumberOfLanes(3.0);
                            link.setCapacity(6000.0);
                            link.setFreespeed(120.0 / 3.6);
                        }else if((highwayType.equals("motorway") || highwayType.equals("trunk"))){
                            link.setFreespeed(120.0 / 3.6);
                        }
                    }
				})
				.build();

		// the actual work is done in this call. Depending on the data size this may take a long time
		Network network = reader.read(input.toString());

		// clean the network to remove unconnected parts where agents might get stuck
		new NetworkCleaner().run(network);

		// write out the network into a file
		new NetworkWriter(network).write("E:/IDEA/SHUJU/matsim-example-project-2024/scenarios/equil/networkPanYuTest.xml.gz");
	}
}
