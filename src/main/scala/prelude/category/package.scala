package prelude

package object category extends CategoryImports

trait CategoryImports extends {}
  with cats.instances.AllInstances
  with cats.syntax.AllSyntax
  with mouse.AllSyntax
