package eu.ubipol.opinionmining.owl_engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

class OwlFile {
  private OWLOntologyManager manager;
  private IRI documentIRI;
  private IRI ontologyIRI;
  private OWLOntology ontology;

  // [start] Static Members
  public static OwlFile PrepareOntologyFile(String ontologyName, String path)
      throws OWLOntologyCreationException, OWLOntologyStorageException {

    File file = new File(path);
    if (file.exists())
      return (new OwlFile(file, ontologyName));
    else {
      System.out.println("Don't have an owl file : " + path);
      return new OwlFile(ontologyName, path);
    }
    // return (new OwlFile(ontologyName, ontologyId));
  }

  // [end]

  // [start] Constructors
  private OwlFile(String ontologyName, String path) throws OWLOntologyCreationException,
      OWLOntologyStorageException {
    manager = OWLManager.createOWLOntologyManager();

    String ontologyIRIString = "http://www.co-ode.org/ontologies/" + ontologyName + ".owl";
    ontologyIRI = IRI.create(ontologyIRIString);
    File file = new File(path);
    documentIRI = IRI.create(file.toURI());
    SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);

    manager.addIRIMapper(mapper);

    ontology = manager.createOntology(ontologyIRI);

    manager.saveOntology(ontology);

    this.AddClass(Utils.GetDomainString(ontologyName));
    this.AddSubClass(Utils.GetDomainString(ontologyName), Utils.POLARITY_KEYWORD);
    this.AddSubClass(Utils.GetDomainString(ontologyName), Utils.ASPECT_KEYWORD);
    // this.AddSubClass(Utils.GetDomainString(ontologyName), Utils.COMMENT_KEYWORD);
    // this.AddSubClass(Utils.GetDomainString(ontologyName), Utils.SCORECARD_KEYWORD);

    this.AddSubClass(Utils.POLARITY_KEYWORD, Utils.ADJECTIVE_KEYWORD);
    this.AddSubClass(Utils.POLARITY_KEYWORD, Utils.ADVERB_KEYWORD);
    this.AddSubClass(Utils.POLARITY_KEYWORD, Utils.VERB_KEYWORD);
    this.AddSubClass(Utils.POLARITY_KEYWORD, Utils.NOUN_KEYWORD);

  }

  private OwlFile(File owlfile, String ontologyName) throws OWLOntologyCreationException {
    manager = OWLManager.createOWLOntologyManager();
    ontology = manager.loadOntologyFromOntologyDocument(owlfile);
    documentIRI = manager.getOntologyDocumentIRI(ontology);
    String ontologyIRIString = "http://www.co-ode.org/ontologies/" + ontologyName + ".owl";
    ontologyIRI = IRI.create(ontologyIRIString);
  }

  // [end]

  // [start] Owl Creation Members
  public void SaveOntology() throws OWLOntologyStorageException {
    manager.saveOntology(ontology);
  }

  public void AddClass(String axiomName) throws OWLOntologyStorageException {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLClass clsAMethodA = factory.getOWLClass(IRI.create(ontologyIRI + "#" + axiomName));
    OWLDeclarationAxiom declarationAxiom = factory.getOWLDeclarationAxiom(clsAMethodA);
    manager.addAxiom(ontology, declarationAxiom);
  }

  public void AddSubClass(String parentClassName, String childClassName)
      throws OWLOntologyStorageException {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLClass parentClass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + parentClassName));
    OWLClass childClass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + childClassName));
    OWLAxiom axiom = factory.getOWLSubClassOfAxiom(childClass, parentClass);
    AddAxiom addAxiom = new AddAxiom(ontology, axiom);
    manager.applyChange(addAxiom);
  }

  public void AddInstanceToClass(String instanceName, String className)
      throws OWLOntologyStorageException {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLClass ontologyClass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + className));
    OWLNamedIndividual ontologyInstance = factory.getOWLNamedIndividual(IRI.create(ontologyIRI
        + "#" + instanceName));
    OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(ontologyClass,
        ontologyInstance);
    manager.addAxiom(ontology, classAssertion);
  }

  public void AddValueToInstance(String instanceName, double value, String valueName)
      throws OWLOntologyStorageException {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#"
        + instanceName));
    OWLDataProperty valueInstance = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#"
        + valueName));
    OWLDataPropertyAssertionAxiom assertion = factory.getOWLDataPropertyAssertionAxiom(
        valueInstance, instance, value);
    manager.addAxiom(ontology, assertion);
  }

  public void AddValueToInstance(String instanceName, int value, String valueName)
      throws OWLOntologyStorageException {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#"
        + instanceName));
    OWLDataProperty valueInstance = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#"
        + valueName));
    OWLDataPropertyAssertionAxiom assertion = factory.getOWLDataPropertyAssertionAxiom(
        valueInstance, instance, value);
    manager.addAxiom(ontology, assertion);
  }

  public void AddValueToInstance(String instanceName, boolean value, String valueName)
      throws OWLOntologyStorageException {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#"
        + instanceName));
    OWLDataProperty valueInstance = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#"
        + valueName));
    OWLDataPropertyAssertionAxiom assertion = factory.getOWLDataPropertyAssertionAxiom(
        valueInstance, instance, value);
    manager.addAxiom(ontology, assertion);
  }

  public void AddValueToInstance(String instanceName, float value, String valueName)
      throws OWLOntologyStorageException {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#"
        + instanceName));
    OWLDataProperty valueInstance = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#"
        + valueName));
    OWLDataPropertyAssertionAxiom assertion = factory.getOWLDataPropertyAssertionAxiom(
        valueInstance, instance, value);
    manager.addAxiom(ontology, assertion);
  }

  public void AddValueToInstance(String instanceName, String value, String valueName)
      throws OWLOntologyStorageException {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLIndividual instance = factory.getOWLNamedIndividual(IRI.create(ontologyIRI + "#"
        + instanceName));
    OWLDataProperty valueInstance = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#"
        + valueName));
    OWLDataPropertyAssertionAxiom assertion = factory.getOWLDataPropertyAssertionAxiom(
        valueInstance, instance, value);
    manager.addAxiom(ontology, assertion);
  }

  // [end]

  // [start] Owl Reading Members
  public List<String> GetNamesOfSubClasses(String className) {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLClass parentClass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + className));
    Set<OWLClassExpression> subClasses = parentClass.getSubClasses(ontology);
    List<String> result = new ArrayList<String>();
    for (OWLClassExpression owlc : subClasses) {
      String s = owlc.toString();
      s = s.substring(s.lastIndexOf('#') + 1);
      s = s.substring(0, s.length() - 1);
      result.add(s);
    }
    return result;
  }

  public List<String> GetNamesOfParentClasses(String className) {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLClass childClass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + className));
    Set<OWLClassExpression> superClasses = childClass.getSuperClasses(ontology);
    List<String> result = new ArrayList<String>();
    for (OWLClassExpression owlc : superClasses) {
      String s = owlc.toString();
      s = s.substring(s.lastIndexOf('#') + 1);
      s = s.substring(0, s.length() - 1);
      result.add(s);
    }
    return result;
  }

  public List<String> GetNamesOfInstances(String className) {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLClass parentClass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + className));
    Set<OWLIndividual> instances = parentClass.getIndividuals(ontology);
    List<String> result = new ArrayList<String>();
    for (OWLIndividual owli : instances) {
      String s = owli.toString();
      s = s.substring(s.lastIndexOf('#') + 1);
      s = s.substring(0, s.length() - 1);
      result.add(s);
    }
    return result;
  }

  public List<String> GetNamesOfValues(String className, String instanceName) {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLClass parentClass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + className));
    Set<OWLIndividual> instances = parentClass.getIndividuals(ontology);
    List<String> result = new ArrayList<String>();

    for (OWLIndividual owli : instances) {
      String s = owli.toString();
      s = s.substring(s.lastIndexOf('#') + 1);
      s = s.substring(0, s.length() - 1);
      if (s.equals(instanceName)) {
        Map<OWLDataPropertyExpression, Set<OWLLiteral>> properties = owli
            .getDataPropertyValues(ontology);
        for (OWLDataPropertyExpression owlp : properties.keySet()) {
          String p = owlp.toString();
          p = p.substring(p.lastIndexOf('#') + 1);
          p = p.substring(0, p.length() - 1);
          result.add(p);
        }
      }
    }
    return result;
  }

  public String GetValueOfADataProperty(String className, String instanceName, String valueName) {
    OWLDataFactory factory = manager.getOWLDataFactory();
    OWLClass parentClass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + className));
    Set<OWLIndividual> instances = parentClass.getIndividuals(ontology);

    for (OWLIndividual owli : instances) {
      String s = owli.toString();
      s = s.substring(s.lastIndexOf('#') + 1);
      s = s.substring(0, s.length() - 1);
      if (s.equals(instanceName)) {
        Map<OWLDataPropertyExpression, Set<OWLLiteral>> properties = owli
            .getDataPropertyValues(ontology);
        for (OWLDataPropertyExpression owlp : properties.keySet()) {
          String p = owlp.toString();
          p = p.substring(p.lastIndexOf('#') + 1);
          p = p.substring(0, p.length() - 1);
          if (p.equals(valueName))
            for (OWLLiteral owll : properties.get(owlp)) {
              String l = owll.toString();
              l = l.substring(1);
              l = l.substring(0, l.lastIndexOf('\"'));
              return l;
            }
        }
      }
    }
    return null;
  }

  // [end]

  // [start] Owl Control Members
  public boolean HasInstance(String instanceName, String className) {
    return this.GetNamesOfInstances(className).contains(instanceName);
  }

  public boolean HasClass(String parentClass, String searchClass) {
    return this.GetNamesOfSubClasses(parentClass).contains(searchClass);
  }
  // [end]
}