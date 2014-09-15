package de.fh.aachen.bpmn.rest;

public interface ConsumerTaskResult {
	/**
	 * This method returns the type of this wrapped result object.
	 * 
	 * @return Type of the result (see MIME-Types as a reference)
	 */
	public String getType();

	/**
	 * This method returns the actual value. Return value can be null.
	 * 
	 * @return Result for the consume task (can be null)
	 */
	public Object getValue();
}
