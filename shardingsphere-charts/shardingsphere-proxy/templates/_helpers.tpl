{{- define "zkAddr" }}
{{- if .Values.zkAddr -}}
{{ .Values.zkAddr -}}
{{- else -}}
127.0.0.1:2181
{{- end -}}
{{ end  }}

{{- define "mysql" }}
{{- with .Values.mysql -}}
addr: {{ .addr | default "127.0.0.1:3306" -}}
username: {{ .username | default "root" }}
passwd: {{ .passwd | default "root" }}
{{- end -}}
{{ end }}

