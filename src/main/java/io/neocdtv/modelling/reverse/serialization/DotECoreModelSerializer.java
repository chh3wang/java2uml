package io.neocdtv.modelling.reverse.serialization;

import com.thoughtworks.qdox.model.JavaClass;
import io.neocdtv.modelling.reverse.model.custom.Classifier;
import io.neocdtv.modelling.reverse.model.custom.Visibility;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.HashSet;
import java.util.Set;

public class DotECoreModelSerializer {

	private final boolean doPackages = false;
	private final boolean doConstants = false;
	private Set<JavaClass> qClasses = new HashSet<>();

	public String start(final Set<EPackage> ePackages, final Set<JavaClass> qClasses) {
		// COMMENT: qClasses are selected to be rendered
		this.qClasses = qClasses;

		StringBuilder dot = new StringBuilder();
		dot.append("digraph G {\n");
		configureLayout(dot);

		doPackagesWithClassifiers(ePackages, dot);
		doRelations(ePackages, dot);

		dot.append("}");
		return dot.toString();
	}

	private void doPackagesWithClassifiers(final Set<EPackage> ePackages, StringBuilder dot) {
		for (EPackage ePackage : ePackages) {
			doPackageWithClassifiers(dot, ePackage);
		}
	}

	private void doRelations(Set<EPackage> ePackages, StringBuilder dot) {
		for (EPackage ePackage : ePackages) {
			for (EClassifier eClassifier : ePackage.getEClassifiers()) {
				if (eClassifier instanceof EClass) {
					((EClass) eClassifier).getEReferences().forEach(eReference -> {
						doRelation(eClassifier, eReference, dot);
					});
				}
			}
			doPackageWithClassifiers(dot, ePackage);
		}
	}

	private void doRelation(EClassifier eClassifier, final EReference relation, final StringBuilder dot) {

		if (isClassifierPackageAvailable(relation.getEReferenceType())) {
			dot.append("\t");
			dot.append("\"").append(relation.getEReferenceType().getInstanceClassName()).append("\"");
			dot.append(" -> ");
			dot.append("\"").append(eClassifier.getInstanceClassName()).append("\"");

			doDependency(relation, dot);

			dot.append("\n");
		}
	}


	private boolean isClassifierPackageAvailable(final EClassifier eClassifier) {
		/*
		for (Classifier current : this.classes) {
			if (current.getId().equals(eClassifier.getId())) {
				return true;
			}
		}
		return false;
		*/
		return true;
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

	private void doPackageWithClassifiers(final StringBuilder dot, final EPackage ePackage) {
		if (doPackages) {
			// TODO: how to render package in dot, can subraphs be configured with shape like nodes and edges?
			dot.append("\t");
			dot.append("subgraph ");
			dot.append("\"").append(ePackage.getName()).append("\"");
			dot.append(" {\n");
			dot.append("\t\t" + "label = \"").append(ePackage.getNsPrefix()).append("\"\n");
		}

		ePackage.getEClassifiers().forEach(eClassifier -> {
			if (eClassifier instanceof EClass) {
				doClass(dot, (EClass) eClassifier);
			} else if (eClassifier instanceof EEnum) {
				doEnumeration(dot, (EEnum) eClassifier);
			}
		});
		if (doPackages) {
			dot.append("\t}\n");
		}
	}

	private void doClass(final StringBuilder dot, final EClass eClass) {
		dot.append("\t\t");
		dot.append("\"").append(eClass.getInstanceClassName()).append("\"");
		dot.append(" [\n");
		dot.append("\t\t\tlabel = ");
		dot.append("\"{");
		dot.append(eClass.getName());
		dot.append("|");

		for (EStructuralFeature eStructuralFeature : eClass.getEStructuralFeatures()) {
			if (eStructuralFeature instanceof EAttribute) {
				doAttribute(dot, (EAttribute) eStructuralFeature);
			}
		}

		/*
		node.getRelations().stream().filter(relation -> relation.getRelationType().equals(RelationType.DEPEDENCY)).forEach(relation -> {
			final Classifier toNode = relation.getToNode();
			if (!isClassifierPackageAvailable(toNode) && (!relation.isToNodeLabelConstant() || doConstants)) {
				doClassifierAsAttribute(dot, toNode, relation.getToNodeLabel(), relation.getToNodeVisibility());
			}
		});
		*/
		//dot.append("|"); - methods
		dot.append("}\"\n\t\t]\n");
	}

	private void doEnumeration(final StringBuilder dot, final EEnum eEnum) {
		dot.append("\t\t");
		dot.append("\"").append(eEnum.getInstanceClassName()).append("\"");
		dot.append(" [\n");
		dot.append("\t\t\tlabel = ");
		dot.append("\"{");
		dot.append(eEnum.getName());
		dot.append("|");
		for (EEnumLiteral eEnumLiteral : eEnum.getELiterals()) {
			dot.append(eEnumLiteral.getLiteral());
			dot.append("\\l");
		}
		//dot.append("|"); - methods
		dot.append("}\"\n\t\t]\n");
	}

	private void doAttribute(StringBuilder dot, EAttribute eAttribute) {
		//dot.append(doVisibility(attribute.getVisibility()));
		//dot.append(" ");
		dot.append(eAttribute.getName());
		dot.append(" : ");
		dot.append(eAttribute.getEAttributeType().getName());
		dot.append("\\l");
	}

	private void doClassifierAsAttribute(final StringBuilder dot, final Classifier classifier, final String name) {
		dot.append(name);
		dot.append(" : ");
		dot.append(classifier.getLabel());
		dot.append("\\l");
	}

	private void doInterfaceImplementation(StringBuilder dot) {
		dot.append(" [dir=back, style=dashed, arrowtail=empty]");
	}

	private void doInheritance(StringBuilder dot) {
		dot.append(" [dir=back, arrowtail=empty]");
	}

	private void doDependency(final EReference eReference, StringBuilder dot) {
		String label = eReference.getName();
		if (eReference.isContainment()) {
			label = label + " \n[0..*]";

		}
		dot.append(String.format(" [dir=back, arrowtail=open ,taillabel=\"%s\"]", label)); // COMMENT: play also with labelangle=\"-7\"
	}
}