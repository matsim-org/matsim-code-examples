package org.matsim.codeexamples.mdp;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.List;

public interface IPolicy {

    public Id<Link> getBestOutgoingLink(MDPState mdpState, Id<Link> currentLink);

    public void updatePolicy(List<Experience> experiences);
}
