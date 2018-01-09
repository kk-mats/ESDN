package enshud.s3.checker;

public interface ASTVisitor
{
	void visit(ASTAssignmentStatement n) throws ASTException;

	void visit(ASTBlock n) throws ASTException;

	void visit(ASTCompoundStatement n) throws ASTException;

	void visit(ASTExpression n) throws ASTException;

	void visit(ASTSimpleExpression n) throws ASTException;

	void visit(ASTTerm n) throws ASTException;

	void visit(ASTFactor n) throws ASTException;

	void visit(ASTIfThenElseStatement n) throws ASTException;

	void visit(ASTIfThenStatement n) throws ASTException;

	void visit(ASTIndexedVariable n) throws ASTException;

	void visit(ASTInputStatement n) throws ASTException;

	void visit(ASTOutputStatement n) throws ASTException;

	void visit(ASTParameter n) throws ASTException;

	void visit(ASTProcedureCallStatement n) throws ASTException;

	void visit(ASTProgram n) throws ASTException;

	void visit(ASTPureVariable n) throws ASTException;

	void visit(ASTSubprogramDeclaration n) throws ASTException;

	void visit(ASTVariableDeclaration n) throws ASTException;

	void visit(ASTVariableType n) throws ASTException;

	void visit(ASTWhileDoStatement n) throws ASTException;
}
