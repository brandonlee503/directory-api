package edu.oregonstate.mist.directoryapi

import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.AuthenticatedUser
import io.dropwizard.auth.Auth
import org.ldaptive.LdapException
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.ResponseBuilder
import javax.ws.rs.core.MediaType

/**
 * DirectoryEntity Resource class.
 */
@Path('/directory')
class DirectoryEntityResource extends Resource {
    private final DirectoryEntityDAO directoryEntityDAO

    /**
     * Constructs the object after receiving and storing directoryEntityDAO instance.
     */
    public DirectoryEntityResource(DirectoryEntityDAO directoryEntityDAO) {
        this.directoryEntityDAO = directoryEntityDAO
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBySearchQuery(
            @Auth AuthenticatedUser authenticatedUser,
            @QueryParam('q') String searchQuery) {
        ResponseBuilder responseBuilder
        if (searchQuery == null || searchQuery.isEmpty()) {
            responseBuilder = badRequest('Missing query parameter.')
        } else {
            try {
                List<DirectoryEntity> directoryEntityList = directoryEntityDAO.getBySearchQuery(searchQuery)
                responseBuilder = ok(directoryEntityList)
            } catch (LdapException ldapException) {
                responseBuilder = internalServerError('LDAP Exception')
            }
        }
        responseBuilder.build()
    }

    @GET
    @Path('/{osuuid: \\d+}')
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByOSUUID(
            @Auth AuthenticatedUser authenticatedUser,
            @PathParam('osuuid') Long osuuid) {
        ResponseBuilder responseBuilder
        try {
            DirectoryEntity directoryEntity = directoryEntityDAO.getByOSUUID(osuuid)
            if (directoryEntity != null) {
                responseBuilder = ok(directoryEntity)
            } else {
                responseBuilder = notFound()
            }
        } catch (LdapException ldapException) {
            responseBuilder = internalServerError('LDAP Exception')
        }
        responseBuilder.build()
    }
}
