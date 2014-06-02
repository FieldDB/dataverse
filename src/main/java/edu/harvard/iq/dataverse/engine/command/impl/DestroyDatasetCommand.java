package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.DataFile;
import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.DatasetVersion;
import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseRole;
import edu.harvard.iq.dataverse.DataverseUser;
import edu.harvard.iq.dataverse.RoleAssignment;
import edu.harvard.iq.dataverse.engine.Permission;
import edu.harvard.iq.dataverse.engine.command.AbstractVoidCommand;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.RequiredPermissions;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;

/**
 * Same as {@link DeleteDatasetCommand}, but does not stop it the dataset is published.
 * This command is reserved for super-users, if at all.
 * @author michael
 */
@RequiredPermissions( Permission.DestructiveEdit )
public class DestroyDatasetCommand extends AbstractVoidCommand {
    
    private final Dataset doomed;

	public DestroyDatasetCommand(Dataset doomed, DataverseUser aUser) {
		super(aUser, doomed.getOwner());
		this.doomed = doomed;
	}
	
	@Override
	protected void executeImpl(CommandContext ctxt) throws CommandException {
		
		final Dataset managedDoomed = ctxt.em().merge(doomed);
		
        // ASSIGNMENTS
		for ( RoleAssignment ra : ctxt.roles().directRoleAssignments(doomed) ) {
			ctxt.em().remove(ra);
		}
		// ROLES
		for ( DataverseRole ra : ctxt.roles().findByOwnerId(doomed.getId()) ) {
			ctxt.em().remove(ra);
		}
        
		// files
		for ( DataFile df : managedDoomed.getFiles() ) {
			ctxt.engine().submit( new DeleteDataFileCommand(df, getUser()) );
		}
		
		// versions
		for ( DatasetVersion ver : managedDoomed.getVersions() ) {
			DatasetVersion managed = ctxt.em().merge(ver);
			ctxt.em().remove( managed );
		}
		
        Dataverse toReIndex = managedDoomed.getOwner();
     
		// dataset
		ctxt.em().remove(managedDoomed);
        
        ctxt.index().indexDataverse(toReIndex);
	}
	
}