package java

import (
	"testing"
)

func TestFunctionName(t *testing.T) {

	j := java{}
	name := j.FieldName("Domain", false)
	if name != "domain" {
		t.Errorf("Expected domain, got %s", name)
	}
}
