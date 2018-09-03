/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.serialization;

import io.neocdtv.modelling.reverse.model.Classifier;
import io.neocdtv.modelling.reverse.model.Attribute;
import io.neocdtv.modelling.reverse.model.Clazz;
import io.neocdtv.modelling.reverse.model.Enumeration;
import io.neocdtv.modelling.reverse.model.Model;
import io.neocdtv.modelling.reverse.model.Relation;
import io.neocdtv.modelling.reverse.model.RelationType;
import io.neocdtv.modelling.reverse.model.Visibility;
import io.neocdtv.modelling.reverse.model.Package;

import java.util.HashSet;
import java.util.Set;

public class DotSerializer implements Serializer {

	private final boolean renderPackages = false;
	private final boolean renderConstants = false; // Underscore uppercase, final, static
	private Set<Classifier> classes = new HashSet<>();

	@Override
	public String renderer(final Model model) {
		// ugly hack, to remove classes from non used/not loaded packages
		model.getPackages().forEach(aPackage -> {
			classes.addAll(aPackage.getClassifiers());
		});
		StringBuilder dot = new StringBuilder();
		dot.append("digraph G {\n");
		configureLayout(dot);
		for (final Package packageToRender : model.getPackages()) {
			renderPackage(dot, packageToRender);
		}
		dot.append("}");
		return dot.toString();
	}

	private void configureLayout(StringBuilder dot) {
		dot.append("\tfontname  = \"Courier\"\n");
		dot.append("\tfontsize  = 8\n");
		dot.append("\tnodesep=0.9\n");
		dot.append("\tranksep=0.9\n");
		dot.append("\tsplines=polyline\n");
		//dot.append("\tsep=\"+50,50\"\n"); // meaning ??
		dot.append("\toverlap=scalexy\n");
		dot.append("\tnode [\n");
		dot.append("\t\tfontname = \"Courier\"\n");
		dot.append("\t\tfontsize  = 8\n");
		dot.append("\t\tshape  = \"record\"\n");
		dot.append("\t]\n");
		dot.append("\tedge [\n");
		dot.append("\t\tfontname = \"Courier\"\n");
		dot.append("\t\tfontsize  = 8\n");
		dot.append("\t]\n");
	}

	private void renderPackage(final StringBuilder dot, final Package packageToRender) {
		if (renderPackages) {
			dot.append("\t");
			dot.append("subgraph ");
			dot.append("\"").append(packageToRender.getId()).append("\"");
			dot.append(" {\n");
			dot.append("\t\t" + "label = \"").append(packageToRender.getLabel()).append("\"\n");
		}

		packageToRender.getClassifiers().forEach(classifier -> {
			if (classifier instanceof Clazz) {
				renderClass(dot, (Clazz) classifier);
			} else if (classifier instanceof Enumeration) {
				renderEnumeration(dot, (Enumeration) classifier);
			}
			classifier.getRelations().forEach(relation -> rendererRelation(dot, relation));
		});
		if (renderPackages) {
			dot.append("\t}\n");
		}
	}

	private void renderEnumeration(final StringBuilder dot, final Enumeration enumeration) {
		dot.append("\t\t");
		dot.append("\"").append(enumeration.getId()).append("\"");
		dot.append(" [\n");
		dot.append("\t\t\tlabel = ");
		dot.append("\"{");
		dot.append(enumeration.getLabel());
		dot.append("|");
		for (String enumConstant : enumeration.getConstants()) {
			dot.append(enumConstant);
			dot.append("\\l");
		}
		dot.append("|");
		dot.append("}\"\n\t\t]\n");
	}

	private void renderClass(final StringBuilder dot, final Clazz node) {
		dot.append("\t\t");
		dot.append("\"").append(node.getId()).append("\"");
		dot.append(" [\n");
		dot.append("\t\t\tlabel = ");
		dot.append("\"{");
		dot.append(node.getLabel());
		dot.append("|");
		for (Attribute attribute : node.getAttributes()) {
			if (!attribute.isConstant()) {
				renderAttribute(dot, attribute);
			}
		}
		node.getRelations().stream().filter(relation -> relation.getRelationType().equals(RelationType.DEPEDENCY)).forEach(relation -> {
			final Classifier toNode = relation.getToNode();
			if (!isClassSourceAvailable(toNode)) {
				renderClassifierAsAttribute(dot, toNode, relation.getToNodeLabel());
			}
		});
		for (Attribute attribute : node.getAttributes()) {
			if (!attribute.isConstant()) {
				renderAttribute(dot, attribute);
			}
		}
		dot.append("|");
		dot.append("}\"\n\t\t]\n");
	}

	private void renderAttribute(StringBuilder dot, Attribute attribute) {
		dot.append(rendererVisibility(attribute.getVisibility()));
		dot.append(" ");
		dot.append(attribute.getName());
		dot.append(" : ");
		dot.append(attribute.getType());
		dot.append("\\l");
	}

	private void renderClassifierAsAttribute(StringBuilder dot, Classifier classifier, String name) {
		dot.append(" ");
		dot.append(name);
		dot.append(" : ");
		dot.append(classifier.getLabel());
		dot.append("\\l");
	}

	private void rendererRelation(final StringBuilder dot, final Relation relation) {
		if (isClassSourceAvailable(relation.getToNode())) {
			dot.append("\t");
			dot.append("\"").append(relation.getToNode().getId()).append("\"");
			dot.append(" -> ");
			dot.append("\"").append(relation.getFromNode().getId()).append("\"");
			switch (relation.getRelationType()) {
				case INTERFACE_REALIZATION:
					rendererInterfaceImplemenatation(dot);
					break;
				case INHERITANCE:
					rendererInheritance(dot);
					break;
				case DEPEDENCY:
					rendererDependency(relation, dot);
					break;
			}
			dot.append("\n");
		}
	}

	private boolean isClassSourceAvailable(final Classifier clazz) {
		for (Classifier current : this.classes) {
			if (current.getId().equals(clazz.getId())) {
				return true;
			}
		}
		return false;
	}

	private String rendererVisibility(final Visibility visibility) {
		switch (visibility) {
			case PRIVATE:
				return "-";
			case PROTECTED:
				return "#";
			case PUBLIC:
				return "+";
		}
		return "";
	}

	private void rendererInterfaceImplemenatation(StringBuilder dot) {
		dot.append(" [dir=back, style=dashed, arrowtail=empty]");
	}

	private void rendererInheritance(StringBuilder dot) {
		dot.append(" [dir=back, arrowtail=empty]");
	}

	private void rendererDependency(Relation relation, StringBuilder dot) {
		switch (relation.getDirection()) {
			case BI: // still in progress see: method updateRelationsDirection
				final String toNodeLabel = relation.getToNodeLabel();
				final String fromNodeLabel = relation.getFromNodeLabel();
				dot.append(String.format(" [dir=none, arrowtail=empty, taillabel=\" %s\", headlabel=\" %s \"]", toNodeLabel, fromNodeLabel));
				break;
			case UNI:
				String label = relation.getToNodeLabel();
				if (relation.getToNodeCardinality() != null) {
					label = label + " \n" + relation.getToNodeCardinality();
				}
				dot.append(String.format(" [dir=back, arrowtail=open ,taillabel=\" %s \"]", label)); // play also with labelangle=\"-7\"
				break;
		}
	}
}