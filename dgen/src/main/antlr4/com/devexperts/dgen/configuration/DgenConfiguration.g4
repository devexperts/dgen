grammar DgenConfiguration;

// Parse
fileConfiguration: classRule*;
classCommentConfiguration: (classRuleOptions | methodRule | fieldRule)*;
methodCommentConfiguration: methodRuleOptions*;
fieldCommentConfiguration: fieldRuleOptions*;

// Rules
classRule: CLASS LBRACK (predicate | methodRule | fieldRule | classRuleOptions)* RBRACK;
methodRule: METHOD LBRACK (predicate | methodRuleOptions)* RBRACK;
fieldRule: FIELD LBRACK (predicate | fieldRuleOptions)* RBRACK;

// Options
classRuleOptions: OPTIONS LBRACK (descriptionRetrieveStrategy | annotateClass)* RBRACK;
methodRuleOptions: OPTIONS LBRACK descriptionRetrieveStrategy* RBRACK;
fieldRuleOptions: OPTIONS LBRACK descriptionRetrieveStrategy* RBRACK;

descriptionRetrieveStrategy: DESCRIPTION_RETRIEVE_STRATEGY ASSIGN
    (firstSentenceStrategy | firstParagraphStrategy | returnTagStrategy | allStrategy) END;
firstSentenceStrategy: FIRST_SENTENCE_STRATEGY;
firstParagraphStrategy: FIRST_PARAGRAPH_STRATEGY;
returnTagStrategy: RETURN_TAG_STRATEGY;
allStrategy: ALL_STRATEGY;

annotateClass: ANNOTATE_CLASS ASSIGN (TRUE | FALSE) END;

// Predicates
predicate: namePredicate | isStaticPredicate | accessModifierPredicate | extendsOrImplementsPredicate;

namePredicate: NAME_PREDICATE ASSIGN name END;

extendsOrImplementsPredicate: EXTENDS_OR_IMPLEMENTS_PREDICATE ASSIGN name END;

isStaticPredicate: IS_STATIC_PREDICATE ASSIGN (TRUE | FALSE) END;

accessModifierPredicate: ACCESS_MODIFIER_PREDICATE ASSIGN accessModifierValue (OR accessModifierValue)* END;
accessModifierValue: PRIVATE | DEFAULT | PROTECTED | PUBLIC;

name: STRING;

// Operators
ASSIGN: '=';

// Keywords
CLASS: 'class';
METHOD: 'method';
FIELD: 'field';
OPTIONS: 'options';

DESCRIPTION_RETRIEVE_STRATEGY: 'retrieveStrategy';
FIRST_SENTENCE_STRATEGY: 'firstSentence';
FIRST_PARAGRAPH_STRATEGY: 'firstParagraph';
RETURN_TAG_STRATEGY: 'returnTag';
ALL_STRATEGY: 'all';

ANNOTATE_CLASS: 'annotateClass';

NAME_PREDICATE: 'name';
EXTENDS_OR_IMPLEMENTS_PREDICATE: 'extendsOrImplements';
IS_STATIC_PREDICATE: 'isStatic';
ACCESS_MODIFIER_PREDICATE: 'access';

PRIVATE: 'private';
DEFAULT: 'default';
PROTECTED: 'protected';
PUBLIC: 'public';

// Constants
STRING: '"' ( ~'"' | '\\' '"' )* '"';
TRUE: 'true';
FALSE: 'false';

// Separators
LBRACK: '{';
RBRACK: '}';
OR: '|';
END: ';';

// Whitespaces and comments
fragment
COMMENT: '#'~[\r\n]*;

fragment
WS: [ \r\t\u000C\n]+;
TRASH: (WS | COMMENT) -> channel(HIDDEN);
