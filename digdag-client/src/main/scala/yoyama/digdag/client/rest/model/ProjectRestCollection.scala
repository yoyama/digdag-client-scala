package yoyama.digdag.client.rest.model

class ProjectRestCollection(val projects:List[ProjectRest]) {
  def filter(filter:String):List[ProjectRest] = ???
  def fetch(filter:String):ProjectRest = ???
  def fetch(id:Long):ProjectRest = ???
}
