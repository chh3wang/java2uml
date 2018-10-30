package io.neocdtv.modelling.reverse.model.custom;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author xix
 */
public abstract class Classifier {

	// COMMENT: id is package.ClassName
	private final String id;
	// COMMENT: label is ClassName
	private final String label;
	private final String packageName;
	private final String stereotype;
	private final Set<Relation> relations = new HashSet<>();

	public Classifier(String name, final String label, final String packageName) {
		this(name, label, packageName, null);
	}

	public Classifier(String name, final String label, final String packageName, final String stereotype) {
		this.id = name;
		this.label = label;
		this.packageName = packageName;
		this.stereotype = stereotype;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getPackageName() {
		return packageName;
	}

	public boolean hasStereotype() {
		return this.stereotype != null;
	}

	public String getStereotype() {
		return stereotype;
	}

	public void addRelation(Classifier toNode, RelationType relationType, Direction direction) {
		final Relation relation = new Relation(this, toNode, relationType, direction);
		relations.add(relation);
	}

	public void addRelation(Classifier toNode,
	                        RelationType relationType,
	                        Direction direction,
	                        String toNodeLabel,
	                        boolean toNodeCardinalityCollection,
													boolean toNodeConstant,
													Visibility toNodeVisibility) {
		final Relation relation = new Relation(this, toNode, relationType, direction, toNodeLabel, toNodeCardinalityCollection, toNodeConstant, toNodeVisibility);
		relations.add(relation);
	}

	public Set<Relation> getRelations() {
		return relations;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + Objects.hashCode(this.id);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Classifier other = (Classifier) obj;
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		return true;
	}
}