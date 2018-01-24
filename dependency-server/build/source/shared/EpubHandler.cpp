#include "EpubHandler.hpp"

namespace EpubReader {

bool EpubHandler::handleGet(CivetServer *server, struct mg_connection *conn)
{
    mg_send_mime_file(conn, epubFile.c_str(), "application/epub+zip");
    return true;
}

void EpubHandler::setEpubFile(const std::string & epubFile)
{
    this->epubFile = epubFile;
}

}