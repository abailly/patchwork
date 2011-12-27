/**
 * 
 */
package oqube.patchwork.gui;

import java.util.Set;

import salvo.jesus.graph.Edge;
import salvo.jesus.graph.algorithm.GraphFilter;

/**
 * Enumerate all kind of links between two classes (or set of classes). The
 * following kind of links are supported: inherits, implements and uses.
 * 
 * @author nono
 * 
 */
public enum LinkType {
	uses, inherits, implement, calls, follows, implemented, overriden;

	public static class LinkTypeFilter implements GraphFilter {

		private Set<LinkType> links;

		public LinkTypeFilter(Set<LinkType> links2) {
			this.links = links2;
		}

		public void setLinksToFilter(Set<LinkType> links) {
			this.links = links;
		}

		public boolean filter(Object v) {
			return true;
		}

		public boolean filter(Edge e) {
			return links.contains(((LinkType) e.getData()));
		}

	}
}
