#include <CivetServer.h>
#include <string>

namespace EpubReader {

class EpubHandler : public CivetHandler
{
public:
	bool handleGet(CivetServer *server, struct mg_connection *conn);
	void setEpubFile(const std::string & epubFile);

private:
    std::string epubFile;	
};

}