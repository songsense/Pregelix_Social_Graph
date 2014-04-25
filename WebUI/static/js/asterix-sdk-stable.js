function AsterixDBConnection(configuration) {
    this._properties = {};
    this._properties["dataverse"] = "";
    this._properties["mode"] = "synchronous";

    // This is a demo setup related fix. Enabled by proxy to Asterix REST API.
    this._properties["endpoint_root"] = "/";

    var configuration = arguments || {};

    for (var key in configuration) {
        this._properties[key] = configuration[key];
    }

    return this;
}


AsterixDBConnection.prototype.dataverse = function(dataverseName) {
    this._properties["dataverse"] = dataverseName;

    return this;
};


AsterixDBConnection.prototype.query = function(statements, successFn, mode) {
    if ( typeof statements === 'string') {
        statements = [ statements ];
    }

    var m = typeof mode ? mode : "synchronous";

    var query = "use dataverse " + this._properties["dataverse"] + ";\n" + statements.join("\n");

    this._api(
        {
            "query" : query,
            "mode"  : m
        },
        successFn,
        "query"
    );

    return this;
};


AsterixDBConnection.prototype.query_status = function(data, successFn) {

    this._api(
        data,
        successFn,
        "query/status"
    );

    return this;
};


AsterixDBConnection.prototype.query_result = function(data, successFn) {
    this._api(
        data,
        successFn,
        "query/result"
    );

    return this;
};


AsterixDBConnection.prototype.ddl = function(statements, successFn) {
    if ( typeof statements === 'string') {
        statements = [ statements ];
    }

    this._api(
        {
            "ddl" :  "use dataverse " + this._properties["dataverse"] + ";\n" + statements.join("\n")
        },
        successFn,
        "ddl"
    );
}


AsterixDBConnection.prototype.update = function(statements, successFn) {
    if ( typeof statements === 'string') {
        statements = [ statements ];
    }

    this._api(
        {
            "statements" : "use dataverse " + this._properties["dataverse"] + ";\n" + statements.join("\n")
        },
        successFn,
        "update"
    );
}


AsterixDBConnection.prototype._api = function(json, onSuccess, endpoint) {
    var success_fn = onSuccess;
    var endpoint_url = this._properties["endpoint_root"] + endpoint;

    $.ajax({
        type: 'GET',
        url: endpoint_url,
        data : json,
        dataType: "json",
        success: function(data) {
            success_fn(data);
        },
        error: function(xhr, status, error) {
        }
    });

    return this;
};


// Asterix Expressions - Base
function AExpression () {

    this._properties = {};
    this._success = function() {};

    if (arguments.length == 1) {
        this._properties["value"] = arguments[0];
    }

    return this;
}


AExpression.prototype.bind = function(options) {
    var options = options || {};

    if (options.hasOwnProperty("success")) {
        this._success = options["success"];
    }

    if (options.hasOwnProperty("return")) {
        this._properties["return"] = " return " + options["return"].val();
    }
};


AExpression.prototype.run = function(successFn) {
    return this;
};


AExpression.prototype.val = function() {

    var value = "";

    // If there is a dataverse defined, provide it.
    if (this._properties.hasOwnProperty("dataverse")) {
        value += "use dataverse " + this._properties["dataverse"] + ";\n";
    };

    if (this._properties.hasOwnProperty("value")) {
        value += this._properties["value"].toString();
    }

    return value;
};


// @param expressionValue [String]
AExpression.prototype.set = function(expressionValue) {
    this._properties["value"] = expressionValue;
    return this;
};


// AQL Statements
// SingleStatement ::= DataverseDeclaration
//                  | FunctionDeclaration
//                  | CreateStatement
//                  | DropStatement
//                  | LoadStatement
//                  | SetStatement
//                  | InsertStatement
//                  | DeleteStatement
//                  | Query
function InsertStatement(quantifiedName, query) {
    AExpression.call(this);

    var innerQuery = "";
    if (query instanceof AExpression) {
        innerQuery = query.val();
    } else if (typeof query == "object" && Object.getPrototypeOf( query ) === Object.prototype ) {

        var insertStatements = [];
        for (querykey in query) {
            if (query[querykey] instanceof AExpression) {
                insertStatements.push('"' + querykey + '" : ' + query[querykey].val());
            } else if (typeof query[querykey] == "string") {
                insertStatements.push('"' + querykey + '" : ' + query[querykey]);
            } else {
                insertStatements.push('"' + querykey + '" : ' + query[querykey].toString());
            }
        }

        innerQuery = "{" + insertStatements.join(', ') + "}";
    }

    var statement = "insert into dataset " + quantifiedName + "(" + innerQuery + ");";

    AExpression.prototype.set.call(this, statement);

    return this;
}

InsertStatement.prototype = Object.create(AExpression.prototype);
InsertStatement.prototype.constructor = InsertStatement;


// Delete Statement
// DeleteStatement ::= "delete" Variable "from" "dataset" QualifiedName ( "where" Expression )?
function DeleteStatement (variable, quantifiedName, whereExpression) {
    AExpression.call(this);

    var statement = "delete " + variable + " from dataset " + quantifiedName;

    if (whereExpression instanceof AExpression) {
        statement += " where " + whereExpression.val();
    }

    AExpression.prototype.set.call(this, statement);

    return this;
}

DeleteStatement.prototype = Object.create(AExpression.prototype);
DeleteStatement.prototype.constructor = DeleteStatement;

// SetStatement
//
// Grammar
// "set" Identifier StringLiteral
function SetStatement (identifier, stringLiteral) {
    AExpression.call(this);

    var statement = "set " + identifier + ' "' + stringLiteral + '";';

    AExpression.prototype.set.call(this, statement);

    return this;
}

SetStatement.prototype = Object.create(AExpression.prototype);
SetStatement.prototype.constructor = SetStatement;


// Other Expressions

// FunctionExpression
// Parent: AsterixExpression
//
// @param   options [Various],
// @key     function [String], a function to be applid to the expression
// @key     expression [AsterixExpression or AQLClause] an AsterixExpression/Clause to which the fn will be applied
function FunctionExpression() {

    // Initialize superclass
    AExpression.call(this);

    this._properties["function"] = "";
    this._properties["expressions"] = [];

    // Check for fn/expression input
    if (arguments.length >= 2 && typeof arguments[0] == "string") {

        this._properties["function"] = arguments[0];

        for (i = 1; i < arguments.length; i++) {
            if (arguments[i] instanceof AExpression || arguments[i] instanceof AQLClause) {
                this._properties["expressions"].push(arguments[i]);
            } else {
                this._properties["expressions"].push(new AExpression(arguments[i]));
            }
        }
    }

    // Return FunctionCallExpression object
    return this;
}


FunctionExpression.prototype = Object.create(AExpression.prototype);
FunctionExpression.prototype.constructor = FunctionExpression;


FunctionExpression.prototype.val = function () {
    var fn_args = [];
    for (var i = 0; i < this._properties["expressions"].length; i++) {
        fn_args.push(this._properties["expressions"][i].val());
    }

    return this._properties["function"] + "(" + fn_args.join(", ") + ")";
};


// FLWOGRExpression
//
// FLWOGRExpression ::= ( ForClause | LetClause ) ( Clause )* "return" Expression
function FLWOGRExpression (options) {
    // Initialize superclass
    AExpression.call(this);

    this._properties["clauses"] = [];
    this._properties["minSize"] = 0;

    // Bind options and return
    this.bind(options);
    return this;
}


FLWOGRExpression.prototype = Object.create(AExpression.prototype);
FLWOGRExpression.prototype.constructor = FLWOGRExpression;


FLWOGRExpression.prototype.bind = function(options) {
    AExpression.prototype.bind.call(this, options);

    var options = options || {};

    if (options instanceof SetStatement) {
         this._properties["clauses"].push(options);
         this._properties["minSize"] += 1;
    }

    if (this._properties["clauses"].length <= this._properties["minSize"]) {
        // Needs to start with for or let clause
        if (options instanceof ForClause || options instanceof LetClause) {
            this._properties["clauses"].push(options);
        }
    } else {
        if (options instanceof AQLClause) {
            this._properties["clauses"].push(options);
        }
    }

    return this;
};


FLWOGRExpression.prototype.val = function() {
    var value = AExpression.prototype.val.call(this);

    var clauseValues = [];
    for (var c in this._properties["clauses"]) {
        clauseValues.push(this._properties["clauses"][c].val());
    }

    return value + clauseValues.join("\n");// + ";";
};

// Pretty Expression Shorthand

FLWOGRExpression.prototype.ReturnClause = function(expression) {
    return this.bind(new ReturnClause(expression));
};

FLWOGRExpression.prototype.ForClause = function() {
    return this.bind(new ForClause(Array.prototype.slice.call(arguments)));
};

FLWOGRExpression.prototype.LetClause = function() {
    return this.bind(new LetClause(Array.prototype.slice.call(arguments)));
};

FLWOGRExpression.prototype.WhereClause = function() {
    return this.bind(new WhereClause(Array.prototype.slice.call(arguments)));
};

FLWOGRExpression.prototype.and = function() {
    var args = Array.prototype.slice.call(arguments);
    args.push(true);
    return this.bind(new WhereClause().and(args));
};

FLWOGRExpression.prototype.or = function() {
    var args = Array.prototype.slice.call(arguments);
    args.push(true);
    return this.bind(new WhereClause().or(args));
};

FLWOGRExpression.prototype.OrderbyClause = function() {
    return this.bind(new OrderbyClause(Array.prototype.slice.call(arguments)));
};


FLWOGRExpression.prototype.GroupClause = function() {
    return this.bind(new GroupClause(Array.prototype.slice.call(arguments)));
};

FLWOGRExpression.prototype.LimitClause = function() {
    return this.bind(new LimitClause(Array.prototype.slice.call(arguments)));
};

FLWOGRExpression.prototype.DistinctClause = function() {
    return this.bind(new DistinctClause(Array.prototype.slice.call(arguments)));
};

FLWOGRExpression.prototype.AQLClause = function() {
    return this.bind(new AQLClause(Array.prototype.slice.call(arguments)));
};


// AQLClause
//
// Base Clause  ::= ForClause | LetClause | WhereClause | OrderbyClause | GroupClause | LimitClause | DistinctClause
function AQLClause() {
    this._properties = {};
    this._properties["clause"] = "";
    this._properties["stack"] = [];
    if (typeof arguments[0] == 'string') {
        this._properties["clause"] = arguments[0];
    }
    return this;
}

AQLClause.prototype.val = function() {
    var value = this._properties["clause"];

    return value;
};

AQLClause.prototype.bind = function(options) {

    if (options instanceof AQLClause) {
        this._properties["clause"] += " " + options.val();
    }

    return this;
};

AQLClause.prototype.set = function(value) {
    this._properties["clause"] = value;
    return this;
};


// ForClause
//
// Grammar:
// "for" Variable ( "at" Variable )? "in" ( Expression )
//
// @param for_variable [String], REQUIRED, first variable in clause
// @param at_variable [String], NOT REQUIRED, first variable in clause
// @param expression [AsterixExpression], REQUIRED, expression to evaluate
function ForClause(for_variable, at_variable, expression) {
    AQLClause.call(this);

    var parameters = [];
    if (arguments[0] instanceof Array) {
        parameters = arguments[0];
    } else {
        parameters = arguments;
    }

    this._properties["clause"] = "for " + parameters[0];

    if (parameters.length == 3) {
        this._properties["clause"] += " at " + parameters[1];
        this._properties["clause"] += " in " + parameters[2].val();
    } else if (parameters.length == 2) {
        this._properties["clause"] += " in " + parameters[1].val();
    }

    return this;
}

ForClause.prototype = Object.create(AQLClause.prototype);
ForClause.prototype.constructor = ForClause;


// LetClause
//
// Grammar:
// LetClause      ::= "let" Variable ":=" Expression
//
// @param let_variable [String]
// @param expression [AExpression]
//
// TODO Vigorous error checking
function LetClause(let_variable, expression) {
    AQLClause.call(this);

    var parameters = [];
    if (arguments[0] instanceof Array) {
        parameters = arguments[0];
    } else {
        parameters = arguments;
    }

    this._properties["clause"] = "let " + parameters[0] + " := ";
    this._properties["clause"] += parameters[1].val();

    return this;
}

LetClause.prototype = Object.create(AQLClause.prototype);
LetClause.prototype.constructor = LetClause;


// ReturnClause
//
// Grammar:
// return [AQLExpression]
function ReturnClause(expression) {
    AQLClause.call(this);

    this._properties["clause"] = "return ";

    if (expression instanceof AExpression || expression instanceof AQLClause) {
        this._properties["clause"] += expression.val();

    } else if ( typeof expression == "object" && Object.getPrototypeOf( expression ) === Object.prototype ) {

        this._properties["clause"] += "\n{\n";
        var returnStatements = [];
        for (returnValue in expression) {

            if (expression[returnValue] instanceof AExpression) {
                returnStatements.push('"' + returnValue + '" ' + " : " + expression[returnValue].val());
            } else if (typeof expression[returnValue] == "string") {
                returnStatements.push('"' + returnValue + '" ' + " : " + expression[returnValue]);
            }
        }
        this._properties["clause"] += returnStatements.join(",\n");
        this._properties["clause"] += "\n}";

    } else {
        this._properties["clause"] += new AQLClause().set(expression).val();
    }

    return this;
}


ReturnClause.prototype = Object.create(AQLClause.prototype);
ReturnClause.prototype.constructor = ReturnClause;


// WhereClause
//
// Grammar:
// ::= "where" Expression
//
// @param expression [BooleanExpression], pushes this expression onto the stack
function WhereClause(expression) {
    AQLClause.call(this);

    this._properties["stack"] = [];

    if (expression instanceof Array) {
        this.bind(expression[0]);
    } else {
        this.bind(expression);
    }

    return this;
}


WhereClause.prototype = Object.create(AQLClause.prototype);
WhereClause.prototype.constructor = WhereClause;


WhereClause.prototype.bind = function(expression) {
    if (expression instanceof AExpression) {
        this._properties["stack"].push(expression);
    }
    return this;
};


WhereClause.prototype.val = function() {
    var value = "";

    if (this._properties["stack"].length == 0) {
        return value;
    }

    var count = this._properties["stack"].length - 1;
    while (count >= 0) {
        value += this._properties["stack"][count].val() + " ";
        count -= 1;
    }

    return "where " + value;
};


WhereClause.prototype.and = function() {

    var parameters = [];
    if (arguments[0] instanceof Array) {
        parameters = arguments[0];
    } else {
        parameters = arguments;
    }

    var andClauses = [];
    for (var expression in parameters) {

        if (parameters[expression] instanceof AExpression) {
            andClauses.push(parameters[expression].val());
        }
    }

    if (andClauses.length > 0) {
        this._properties["stack"].push(new AExpression().set(andClauses.join(" and ")));
    }

    return this;
};


WhereClause.prototype.or = function() {

    var parameters = [];
    if (arguments[0] instanceof Array) {
        parameters = arguments[0];
    } else {
        parameters = arguments;
    }

    var orClauses = [];
    for (var expression in parameters) {

        if (parameters[expression] instanceof AExpression) {
            orClauses.push(parameters[expression].val());
        }
    }

    if (andClauses.length > 0) {
        this._properties["stack"].push(new AExpression().set(orClauses.join(" and ")));
    }

    return this;
};

// LimitClause
// Grammar:
// LimitClause    ::= "limit" Expression ( "offset" Expression )?
//
// @param   limitExpression [REQUIRED, AQLExpression]
// @param   offsetExpression [OPTIONAL, AQLExpression]
function LimitClause(limitExpression, offsetExpression) {

    AQLClause.call(this);

    var parameters = [];
    if (arguments[0] instanceof Array) {
        parameters = arguments[0];
    } else {
        parameters = arguments;
    }

    // limitExpression required
    this._properties["clause"] = "limit " + parameters[0].val();

    // Optional: Offset
    if (parameters.length == 2) {
        this._properties["clause"] += " offset " + parameters[1].val();
    }

    return this;
}

LimitClause.prototype = Object.create(AQLClause.prototype);
LimitClause.prototype.constructor = LimitClause;


// OrderbyClause
//
// Grammar:
// OrderbyClause  ::= "order" "by" Expression ( ( "asc" ) | ( "desc" ) )? ( "," Expression ( ( "asc" ) | ( "desc" ) )? )*
//
// @params AQLExpressions and asc/desc strings, in any quantity. At least one required.
function OrderbyClause() {

    AQLClause.call(this);

    // At least one argument expression is required, and first should be expression
    if (arguments.length == 0) {
        this._properties["clause"] = null;
        return this;
    }

    var parameters = [];
    if (arguments[0] instanceof Array) {
        parameters = arguments[0];
    } else {
        parameters = arguments;
    }

    var expc = 0;
    var expressions = [];

    while (expc < parameters.length) {

        var expression = "";

        if (parameters[expc] instanceof AExpression) {
            expression += parameters[expc].val();
        }

        var next = expc + 1;
        if (next < parameters.length && (parameters[next] == "asc" || parameters[next] == "desc")) {
            expc++;
            expression += " " + parameters[expc];
        }

        expressions.push(expression);

        expc++;
    }

    this._properties["clause"] = "order by " + expressions.join(", ");
    return this;
}

OrderbyClause.prototype = Object.create(AQLClause.prototype);
OrderbyClause.prototype.constructor = OrderbyClause;


// GroupClause
//
// Grammar:
// GroupClause    ::= "group" "by" ( Variable ":=" )? Expression ( "," ( Variable ":=" )? Expression )* ( "decor" Variable ":=" Expression ( "," "decor" Variable ":=" Expression )* )? "with" VariableRef ( "," VariableRef )*
function GroupClause() {
    AQLClause.call(this);

    if (arguments.length == 0) {
        this._properties["clause"] = null;
        return this;
    }

    var parameters = [];
    if (arguments[0] instanceof Array) {
        parameters = arguments[0];
    } else {
        parameters = arguments;
    }

    var expc = 0;
    var expressions = [];
    var variableRefs = [];
    var isDecor = false;

    while (expc < parameters.length) {

        if (parameters[expc] instanceof AExpression) {

            isDecor = false;
            expressions.push(parameters[expc].val());

        } else if (typeof parameters[expc] == "string") {

            // Special keywords, decor & with
            if (parameters[expc] == "decor") {
                isDecor = true;
            } else if (parameters[expc] == "with") {
                isDecor = false;
                expc++;
                while (expc < parameters.length) {
                    variableRefs.push(parameters[expc]);
                    expc++;
                }

            // Variables and variable refs
            } else {

                var nextc = expc + 1;
                var expression = "";

                if (isDecor) {
                    expression += "decor ";
                    isDecor = false;
                }

                expression += parameters[expc] + " := " + parameters[nextc].val();
                expressions.push(expression);
                expc++;
            }
        }

        expc++;
    }

    this._properties["clause"] = "group by " + expressions.join(", ") + " with " + variableRefs.join(", ");
    return this;
}

GroupClause.prototype = Object.create(AQLClause.prototype);
GroupClause.prototype.constructor = GroupClause;


// Quantified Expression
//
// Grammar
// QuantifiedExpression ::= ( ( "some" ) | ( "every" ) ) Variable "in" Expression ( "," Variable "in" Expression )* "satisfies" Expression
//
// @param String some/every
// @param [AExpression]
// @param [Aexpression] satisfiesExpression
function QuantifiedExpression (keyword, expressions, satisfiesExpression) {
    AExpression.call(this);

    var expression = keyword + " ";
    var varsInExpressions = [];

    for (var varInExpression in expressions) {
        varsInExpressions.push(varInExpression + " in " + expressions[varInExpression].val());
    }
    expression += varsInExpressions.join(", ") + " satisfies " + satisfiesExpression.val();

    AExpression.prototype.set.call(this, expression);

    return this;
}

QuantifiedExpression.prototype = Object.create(AExpression.prototype);
QuantifiedExpression.prototype.constructor = QuantifiedExpression;

QuantifiedExpression.prototype.val = function() {
    var value = AExpression.prototype.val.call(this);
    return "(" + value + ")";
};
